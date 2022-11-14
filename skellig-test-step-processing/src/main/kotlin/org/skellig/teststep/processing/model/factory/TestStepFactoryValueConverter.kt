package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.experiment.ConvertedValueChunkBuilder
import org.skellig.teststep.processing.experiment.ValueProcessingVisitor

open class TestStepFactoryValueConverter private constructor(private val valueProcessingVisitor: ValueProcessingVisitor) {

    companion object {
        private val notToParseValues = mutableSetOf<String>()
    }

    private val converter = ConvertedValueChunkBuilder()

    open fun <T> convertValue(result: Any?, parameters: Map<String, Any?>): T? =
        when (result) {
            is Map<*, *> -> result.entries.associate {
                val newKey = valueProcessingVisitor.process(converter.buildFrom(it.key.toString(), parameters))
                newKey to convertValue<T>(it.value, parameters)
            }
            is Collection<*> -> result.map { convertValue<T>(it, parameters) }.toList()
            is String -> {
                if (!notToParseValues.contains(result)) {
                    val newResult = valueProcessingVisitor.process(converter.buildFrom(result, parameters)) ?: result
                    if (newResult == result) notToParseValues.add(result)
                    newResult
                } else result
            }
            else -> result
        } as T?

    class Builder {
        private var valueProcessingVisitor: ValueProcessingVisitor? = null

        fun withValueProcessingVisitor(valueProcessingVisitor: ValueProcessingVisitor?) =
            apply { this.valueProcessingVisitor = valueProcessingVisitor }

        fun build(): TestStepFactoryValueConverter {
            return TestStepFactoryValueConverter(
                valueProcessingVisitor ?: error("ValueProcessingVisitor is mandatory"),
            )
        }

    }
}