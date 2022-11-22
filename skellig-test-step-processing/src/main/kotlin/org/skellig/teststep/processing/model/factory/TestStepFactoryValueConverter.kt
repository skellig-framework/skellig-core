package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.value.chunk.RawValueChunkParser
import org.skellig.teststep.processing.value.chunk.RawValueProcessingVisitor

open class TestStepFactoryValueConverter private constructor(private val rawValueProcessingVisitor: RawValueProcessingVisitor) {

    companion object {
        private val notToParseValues = mutableSetOf<String>()
    }

    private val rawValueChunkParser = RawValueChunkParser()

    open fun <T> convertValue(result: Any?, parameters: Map<String, Any?>): T? =
        when (result) {
            is Map<*, *> -> result.entries.associate {
                val newKey = rawValueProcessingVisitor.process(rawValueChunkParser.buildFrom(it.key.toString(), parameters))
                newKey to convertValue<T>(it.value, parameters)
            }
            is Collection<*> -> result.map { convertValue<T>(it, parameters) }.toList()
            is String -> {
                if (!notToParseValues.contains(result)) {
                    val newResult = rawValueProcessingVisitor.process(rawValueChunkParser.buildFrom(result, parameters)) ?: result
                    if (newResult == result) notToParseValues.add(result)
                    newResult
                } else result
            }
            else -> result
        } as T?

    class Builder {
        private var rawValueProcessingVisitor: RawValueProcessingVisitor? = null

        fun withValueProcessingVisitor(rawValueProcessingVisitor: RawValueProcessingVisitor?) =
            apply { this.rawValueProcessingVisitor = rawValueProcessingVisitor }

        fun build(): TestStepFactoryValueConverter {
            return TestStepFactoryValueConverter(
                rawValueProcessingVisitor ?: error("ValueProcessingVisitor is mandatory"),
            )
        }

    }
}