package org.skellig.teststep.processing.experiment

import org.skellig.teststep.processing.validation.comparator.ValueComparator

interface ConvertedValueChunk {
    fun process(visitor: ValueProcessingVisitor): Any? = visitor.process(this)
}

class SimpleValue(val value: Any?) : ConvertedValueChunk {

    override fun toString(): String {
        return "$value"
    }

}

class CompositeConvertedValue : ConvertedValueChunk {
    val chunks = mutableListOf<ConvertedValueChunk>()
    val extractions = mutableListOf<ConvertedValueChunk>()

    fun append(chunk: ConvertedValueChunk): CompositeConvertedValue {
        chunks.add(chunk)
        return this
    }

    fun appendExtraction(extraction: ConvertedValueChunk): CompositeConvertedValue {
        extractions.add(extraction)
        return this
    }

    override fun toString(): String {
        val joinedChunks = chunks.joinToString("")
        return if (extractions.isEmpty()) {
            joinedChunks
        } else {
            "$joinedChunks.${extractions.joinToString(".")}"
        }
    }
}

class FunctionValue(
    val name: String,
    val args: Array<ConvertedValueChunk?>,
) : ConvertedValueChunk {

    override fun toString(): String {
        return "$name(${args.joinToString(",")})"
    }
}

class PropertyValue(
    val key: String,
    val default: ConvertedValueChunk?,
    val parameters: Map<String, Any?>,
) : ConvertedValueChunk {

    override fun toString(): String {
        return default?.let { "\${$key:$default}" } ?: "\${$key}"
    }
}


open class ValueProcessingVisitor(
    private val functionalValueProcessor: FunctionValueProcessor,
    private val valueExtractor: ValueExtractor,
    private val valueComparator: ValueComparator,
    private val propertyExtractor: PropertyExtractor
) {

    fun process(functionValue: FunctionValue): Any? {
        val finalArgs = functionValue.args.map { process(it) }.toTypedArray()
        return functionalValueProcessor.execute(functionValue.name, finalArgs)
    }

    fun process(propertyValue: PropertyValue): Any? {
        var value = propertyExtractor.extractFrom(propertyValue.key, propertyValue.parameters)
        if (value == null) {
            value = process(propertyValue.default)
        }
        return value
    }

    fun process(compositeValue: CompositeConvertedValue): Any? {
        val value = process(getChunksFromCompositeChunk(compositeValue))
        return extractValue(value, compositeValue.extractions)
    }

    fun process(simpleValue: SimpleValue): Any? {
        return simpleValue.value
    }

    fun process(chunkValue: ConvertedValueChunk?): Any? {
        return when (chunkValue) {
            is FunctionValue -> process(chunkValue)
            is PropertyValue -> process(chunkValue)
            is CompositeConvertedValue -> process(chunkValue)
            is SimpleValue -> process(chunkValue)
            else -> null
        }
    }

    fun process(value: Any?, chunkValue: ConvertedValueChunk?): Any? {
        return when (chunkValue) {
            is CompositeConvertedValue -> {
                // all chunks in this function are extractors too, so we merge them all together
                extractValue(value, listOf(getChunksFromCompositeChunk(chunkValue)) + chunkValue.extractions)
            }
            checkNotNull(chunkValue) -> extractValue(value, listOf(chunkValue))
            else -> value
        }
    }

    fun process(chunkValue: ConvertedValueChunk?, valueToCompare: Any?): Boolean {
        return when (chunkValue) {
            is FunctionValue -> {
                valueComparator.compare(
                    chunkValue.name,
                    chunkValue.args.map { process(it) }.toTypedArray(), valueToCompare
                )
            }
            is PropertyValue, is SimpleValue ->
                valueComparator.compare("", arrayOf(process(chunkValue)), valueToCompare)
            is CompositeConvertedValue -> process(getChunksFromCompositeChunk(chunkValue), valueToCompare)
            else -> chunkValue == valueToCompare
        }
    }

    private fun extractValue(value: Any?, extractionValues: List<ConvertedValueChunk>): Any? {
        var mutValue = value
        for (extractionValue in extractionValues) {
            mutValue = when (extractionValue) {
                is FunctionValue -> valueExtractor.extractFrom(extractionValue.name, mutValue, extractionValue.args.map { process(it) }.toTypedArray())
                else -> valueExtractor.extractFrom("", mutValue, arrayOf(process(extractionValue)))
            }
        }
        return mutValue
    }

    private fun getChunksFromCompositeChunk(compositeValue: CompositeConvertedValue): ConvertedValueChunk =
        if (compositeValue.chunks.size == 1) compositeValue.chunks[0]
        else SimpleValue(compositeValue.chunks.map { process(it) }.joinToString(""))
}

interface FunctionValueProcessor {

    fun execute(name: String, args: Array<Any?>): Any?

    fun getFunctionName(): String
}

interface ValueExtractor {
    fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any?

    fun getExtractFunctionName(): String
}

interface PropertyExtractor {
    fun extractFrom(propertyKey: String, parameters: Map<String, Any?>): Any?
}

class DefaultPropertyExtractor(private val propertyExtractorFunction: ((String) -> Any?)?) : PropertyExtractor {

    override fun extractFrom(propertyKey: String, parameters: Map<String, Any?>): Any? {
        var propertyValue: Any? = null
        if (propertyExtractorFunction != null) {
            propertyValue = propertyExtractorFunction.invoke(propertyKey)
        }
        if (propertyValue == null && parameters.containsKey(propertyKey)) {
            val value = parameters[propertyKey]
            propertyValue = value
//            if (!(value is String && value.isEmpty())) {
//                propertyValue = parser.parse(value, parameters)
//            }
        }
        if (propertyValue == null) {
            propertyValue = System.getProperty(propertyKey)
        }
        if (propertyValue == null) {
            propertyValue = System.getenv(propertyKey)
        }
        return propertyValue
    }
}