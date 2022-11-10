package org.skellig.teststep.processing.experiment

interface ConvertedValueChunk {
    fun process(visitor: ValueProcessingVisitor): Any? = visitor.process(this)
}

class SimpleValue(val value: Any?) : ConvertedValueChunk {

}

class CompositeConvertedValue() : ConvertedValueChunk {
    val chunks = mutableListOf<ConvertedValueChunk>()
    val extractions = mutableListOf<ConvertedValueChunk>()

    fun append(chunk: ConvertedValueChunk) {
        chunks.add(chunk)
    }

    fun appendExtraction(extraction: ConvertedValueChunk) {
        extractions.add(extraction)
    }
}

class FunctionValue(
    val name: String,
    val args: Array<ConvertedValueChunk?>,
) : ConvertedValueChunk {
}

class PropertyValue(
    val key: String,
    val default: ConvertedValueChunk?,
    val parameters: Map<String, Any?>,
) : ConvertedValueChunk {
}


class ValueProcessingVisitor(
    private val functionalValueProcessor: FunctionValueProcessor,
    private val valueExtractor: ValueExtractor,
    private val propertyExtractor: PropertyExtractor
) {

    fun process(functionValue: FunctionValue): Any? {
        val finalArgs = functionValue.args.map { process(it) }.toTypedArray()
        return functionalValueProcessor.process(finalArgs)
    }

    fun process(propertyValue: PropertyValue): Any? {
        var value = propertyExtractor.extractFrom(propertyValue)
        if (value == null) {
            value = process(propertyValue.default)
        }
        return value
    }

    fun process(compositeValue: CompositeConvertedValue): Any? {
        val value = if (compositeValue.chunks.size == 1) {
            process(compositeValue.chunks[0])
        } else {
            compositeValue.chunks.map { process(it) }.joinToString("")
        }
        return extractValue(value, compositeValue.extractions)
    }

    fun process(simpleValue: SimpleValue): Any? {
        return simpleValue.value
    }

    fun process(chunkValue: ConvertedValueChunk?): Any? {
        return null
    }

    private fun extractValue(value: Any?, extractionValues: List<ConvertedValueChunk>): Any? {
        var mutValue = value
        for (extractionValue in extractionValues) {
            mutValue = valueExtractor.extractFrom(mutValue, extractionValue)
        }
        return mutValue
    }
}

interface FunctionValueProcessor {

    fun process(args: Array<Any?>): Any?
}

interface ValueExtractor {
    fun extractFrom(value: Any?, extractionValue: ConvertedValueChunk): Any?
}

interface PropertyExtractor {
    fun extractFrom(propertyValue: PropertyValue): Any?
}