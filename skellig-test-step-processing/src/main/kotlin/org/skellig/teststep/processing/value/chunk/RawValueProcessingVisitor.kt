package org.skellig.teststep.processing.value.chunk

import org.skellig.teststep.processing.value.function.FunctionValueExecutor
import org.skellig.teststep.processing.validation.comparator.ValueComparator
import org.skellig.teststep.processing.value.property.PropertyExtractor
import org.skellig.teststep.processing.value.extractor.ValueExtractor

open class RawValueProcessingVisitor(
    private val functionalValueProcessor: FunctionValueExecutor,
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
                valueComparator.compare(
                    chunkValue.name,
                    chunkValue.args.map { process(it) }.toTypedArray(), valueToCompare
                )
            }
            is PropertyValue, is SimpleValue ->
                valueComparator.compare("", arrayOf(process(chunkValue)), valueToCompare)
            is CompositeRawValue -> process(getChunksFromCompositeChunk(chunkValue), valueToCompare)
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