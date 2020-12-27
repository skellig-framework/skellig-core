package org.skellig.teststep.processing.model

import java.util.*

open class TestStep(
        var id: String? = null,
        val name: String,
        val execution: TestStepExecutionType? = TestStepExecutionType.SYNC,
        val timeout: Int = 0,
        val delay: Int = 0,
        val variables: Map<String, Any?>? = null,
        val testData: Any? = null,
        val validationDetails: ValidationDetails? = null
) {

    var getId: String? = id
        get() = field ?: name

    open class Builder(
            protected var id: String? = null,
            protected var name: String? = null,
            protected var variables: Map<String, Any?>? = null,
            protected var testData: Any? = null,
            protected var validationDetails: ValidationDetails? = null,
            protected var execution: TestStepExecutionType? = null,
            protected var timeout: Int = 0,
            protected var delay: Int = 0) {

        fun withId(id: String?) = apply { this.id = id }

        fun withName(name: String?) = apply { this.name = name }

        fun withTestData(testData: Any?) = apply { this.testData = testData }

        fun withValidationDetails(validationDetails: ValidationDetails?) = apply { this.validationDetails = validationDetails }

        fun withVariables(variables: Map<String, Any?>?) = apply { this.variables = variables }

        fun withExecution(execution: TestStepExecutionType?) = apply { this.execution = execution }

        fun withTimeout(timeout: Int) = apply { this.timeout = timeout }

        fun withDelay(delay: Int) = apply { this.delay = delay }

        open fun build(): TestStep {
            Objects.requireNonNull(name, "Name of the Test Step must be set")
            return TestStep(id, name!!, execution, timeout, delay, variables, testData, validationDetails)
        }
    }
}