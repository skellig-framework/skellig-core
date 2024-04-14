package org.skellig.teststep.processing.model

/**
 * Represents the execution type of test step.
 * This type is used by [TestStepProcessor][org.skellig.teststep.processing.processor.TestStepProcessor] to determine
 * the way of running a test step: synchronously or asynchronously.
 */
enum class TestStepExecutionType {
    SYNC,
    ASYNC;

    companion object {
        fun fromName(name: String?): TestStepExecutionType {
            for (value in entries) {
                if (value.name.equals(name, ignoreCase = true)) {
                    return value
                }
            }
            return SYNC
        }
    }
}