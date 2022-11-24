package org.skellig.teststep.processing.value.chunk

import org.skellig.teststep.processing.validation.comparator.ValueComparator
import org.skellig.teststep.processing.value.extractor.ValueExtractor
import org.skellig.teststep.processing.value.function.FunctionValueExecutor
import org.skellig.teststep.processing.value.property.PropertyExtractor

open class RawValueProcessingVisitor(
    private val functionalValueProcessor: FunctionValueExecutor,
    private val valueExtractor: ValueExtractor,
    private val valueComparator: ValueComparator,
    private val propertyExtractor: PropertyExtractor
) {

    fun process(functionValue: FunctionValue): Any? {
        val finalArgs = functionValue.args.map { process(it) }.toTypedArray()
        // if function is a comparator, then don't process it and return its string + args value
        return if (valueComparator.isApplicable(functionValue.name) && functionValue.name.isNotEmpty())
            "${functionValue.name}(${finalArgs.joinToString(",")})"
        else functionalValueProcessor.execute(functionValue.name, finalArgs)
    }

    fun process(propertyValue: PropertyValue): Any? {
        var value = propertyExtractor.extractFrom(propertyValue.key, propertyValue.parameters)
        if (value == null) {
            value = process(propertyValue.default)
        }
        return value
    }

    fun process(compositeValue: CompositeRawValue): Any? {
        val value = process(getChunksFromCompositeChunk(compositeValue))
        return extractValue(value, compositeValue.extractions)
    }

    fun process(simpleValue: SimpleValue): Any? {
        return simpleValue.value
    }

    fun process(chunkValue: RawValueChunk?): Any? {
        return when (chunkValue) {
            is FunctionValue -> process(chunkValue)
            is PropertyValue -> process(chunkValue)
            is CompositeRawValue -> process(chunkValue)
            is SimpleValue -> process(chunkValue)
            else -> null
        }
    }

    fun process(value: Any?, chunkValue: RawValueChunk?): Any? {
        return when (chunkValue) {
            is CompositeRawValue -> {
                // all chunks in this function are extractors too, so we merge them all together
                extractValue(value, listOf(getChunksFromCompositeChunk(chunkValue)) + chunkValue.extractions)
            }
            checkNotNull(chunkValue) -> extractValue(value, listOf(chunkValue))
            else -> value
        }
    }

    fun process(chunkValue: RawValueChunk?, valueToCompare: Any?): Boolean {
        return when (chunkValue) {
            is FunctionValue -> {
                // if it's a value comparator then call it,
                // otherwise, use a default comparison (ex. equals) on the processed value chunk.
                return if (valueComparator.isApplicable(chunkValue.name)) {
                    valueComparator.compare(
                        chunkValue.name,
                        chunkValue.args.map { process(it) }.toTypedArray(),
                        valueToCompare
                    )
                } else {
                    valueComparator.compare(
                        "",
                        arrayOf(process(chunkValue)),
                        valueToCompare
                    )
                }
            }
            is PropertyValue, is SimpleValue ->
                valueComparator.compare("", arrayOf(process(chunkValue)), valueToCompare)
            is CompositeRawValue -> {
                // if no extractions then it's safe to process the internal chunks of the composite one.
                // Otherwise, we extract the value and compare it with actual one.
                if (chunkValue.extractions.isEmpty()) {
                    process(getChunksFromCompositeChunk(chunkValue), valueToCompare)
                } else {
                    process(chunkValue) == valueToCompare
                }
            }
            else -> chunkValue == valueToCompare
        }
    }

    private fun extractValue(value: Any?, extractionValues: List<RawValueChunk>): Any? {
        var mutValue = value
        for (extractionValue in extractionValues) {
            mutValue = when (extractionValue) {
                is FunctionValue -> valueExtractor.extractFrom(extractionValue.name, mutValue, extractionValue.args.map { process(it) }.toTypedArray())
                else -> valueExtractor.extractFrom("", mutValue, arrayOf(process(extractionValue)))
            }
        }
        return mutValue
    }

    private fun getChunksFromCompositeChunk(compositeValue: CompositeRawValue): RawValueChunk =
        if (compositeValue.chunks.size == 1) compositeValue.chunks[0]
        else SimpleValue(compositeValue.chunks.map { process(it) }.joinToString(""))
}