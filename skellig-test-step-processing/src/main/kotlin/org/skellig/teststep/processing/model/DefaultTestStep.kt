package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.model.validation.ValidationNode

/**
 * A basic test step with common properties and validation details for a result.
 *
 * @param id a unique id of the test step. It is usually used to store its data or a result in the scenario state.
 * @param name name of the test step can be a simple string or a regex, including capture groups as parameters.
 * For example: `run (\\d+) request[s]?`
 * @param execution can be `async` or `sync` (default)
 * @param timeout time in milliseconds which can be used to set a max processing time
 * @param delay delay time in milliseconds used in conjunction with `attempts` property, indicating a delay time
 * between execution attempts of the test step.
 * @param attempts defines max attempts for running the test step before it fails.
 * @param values a list of values of the test step. Value of a variable can be referenced withing the test step
 * by using `${ }` notation.
 * @param testData can be any type of data representing a message or a request used when processing the test step.
 * @param validationDetails defines a structure to validate the processing result of the test step or another test step.
 */
open class DefaultTestStep(
    val id: String? = null,
    override val name: String,
    val execution: TestStepExecutionType? = TestStepExecutionType.SYNC,
    val timeout: Int = 0,
    val delay: Int = 0,
    val attempts: Int = 0,
    val values: Map<String, Any?>? = null,
    val testData: Any? = null,
    val validationDetails: ValidationNode? = null
) : TestStep {

    val getId: String? = id
        get() = field ?: name

    class DefaultTestStepBuilder : Builder<DefaultTestStep>() {
        override fun build(): DefaultTestStep {
            return DefaultTestStep(
                id, name ?: error("Name of the Test Step must be set"),
                execution, timeout, delay, 0, values, testData, validationDetails
            )
        }
    }

    abstract class Builder<T>(
        protected var id: String? = null,
        protected var name: String? = null,
        protected var values: Map<String, Any?>? = null,
        protected var testData: Any? = null,
        protected var validationDetails: ValidationNode? = null,
        protected var execution: TestStepExecutionType? = null,
        protected var timeout: Int = 0,
        protected var delay: Int = 0,
        protected var attempts: Int = 0
    ) {

        fun withId(id: String?) = apply { this.id = id }

        fun withName(name: String?) = apply { this.name = name }

        fun withTestData(testData: Any?) = apply { this.testData = testData }

        fun withValidationDetails(validationDetails: ValidationNode?) = apply { this.validationDetails = validationDetails }

        fun withValues(values: Map<String, Any?>?) = apply { this.values = values }

        fun withExecution(execution: TestStepExecutionType?) = apply { this.execution = execution }

        fun withTimeout(timeout: Int) = apply { this.timeout = timeout }

        fun withDelay(delay: Int) = apply { this.delay = delay }

        fun withAttempts(attempts: Int) = apply { this.attempts = attempts }

        abstract fun build(): T
    }
}