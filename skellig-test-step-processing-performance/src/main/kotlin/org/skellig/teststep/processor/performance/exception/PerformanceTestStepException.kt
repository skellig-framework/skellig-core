package org.skellig.teststep.processor.performance.exception

import org.skellig.teststep.processing.model.TestStep

class PerformanceTestStepException(message: String, error: Throwable?) : RuntimeException(message, error) {
    private var aggregatedErrors: Map<TestStep, RuntimeException>? = null

    constructor(aggregatedErrors: Map<TestStep, RuntimeException>)
            : this(aggregatedErrors.values.joinToString("\n") { it.message ?: "" }, null) {
        this.aggregatedErrors = aggregatedErrors
    }

    fun aggregate(): List<PerformanceTestStepException> {
        return aggregatedErrors?.map { PerformanceTestStepException("Failed to run test step '${it.key.name}'", it.value) }?.toList() ?: emptyList()
    }
}