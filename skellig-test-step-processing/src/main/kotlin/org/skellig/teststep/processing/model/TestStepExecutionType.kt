package org.skellig.teststep.processing.model

enum class TestStepExecutionType {
    SYNC,
    ASYNC;

    companion object {
        fun fromName(name: String?): TestStepExecutionType {
            for (value in values()) {
                if (value.name.equals(name, ignoreCase = true)) {
                    return value
                }
            }
            return SYNC
        }
    }
}