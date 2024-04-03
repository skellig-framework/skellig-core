package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.processor.task.DefaultTaskProcessor
import org.skellig.teststep.processing.processor.task.TaskTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * Processes any test step by assigning an appropriate test step processor from its registry.
 *
 * If no processor found for the provided test step, then it throws `TestStepProcessingException`
 */
class CompositeTestStepProcessor private constructor(
    testScenarioState: TestScenarioState,
    valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
    processTestStepDelegate: (String, Map<String, Any?>) -> TestStepRunResult
) : TestStepProcessor<TestStep> {

    private val testStepProcessors: MutableList<TestStepProcessor<in TestStep>> = mutableListOf()

    init {
        registerTestStepProcessor(
            DefaultTestStepProcessor.Builder()
                .withTestScenarioState(testScenarioState)
                .build()
        )
        registerTestStepProcessor(GroupedTestStepProcessor(this))
        registerTestStepProcessor(
            TaskTestStepProcessor(
                DefaultTaskProcessor(testScenarioState, valueConvertDelegate, processTestStepDelegate),
                testScenarioState
            )
        )
        registerTestStepProcessor(ClassTestStepProcessor(testScenarioState))
    }

    override fun process(testStep: TestStep): TestStepRunResult {

        val testStepProcessor = testStepProcessors.find { testStep.javaClass == it.getTestStepClass() }

        return testStepProcessor?.let { ts ->
            return ts.process(testStep)
        } ?: throw TestStepProcessingException("No processor was found for test step '${testStep.name}'")
    }

    /**
     * Register test step processor.
     */
    fun registerTestStepProcessor(testStepProcessor: TestStepProcessor<out TestStep>) {
        testStepProcessors.add(testStepProcessor as TestStepProcessor<TestStep>)
    }

    override fun getTestStepClass(): Class<TestStep> {
        return TestStep::class.java
    }

    override fun close() {
        testStepProcessors.forEach { it.close() }
    }

    class Builder : BaseTestStepProcessor.Builder<TestStep>() {
        var valueConvertDelegate: ((ValueExpression?, Map<String, Any?>) -> Any?)? = null
        var processTestStepDelegate: ((String, Map<String, Any?>) -> TestStepRunResult)? = null

        fun withValueConvertDelegate(valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?) =
            apply { this.valueConvertDelegate = valueConvertDelegate }

        fun withProcessTestStepDelegate(processTestStepDelegate: (String, Map<String, Any?>) -> TestStepRunResult) =
            apply { this.processTestStepDelegate = processTestStepDelegate }

        override fun build(): CompositeTestStepProcessor {
            return CompositeTestStepProcessor(
                testScenarioState!!,
                valueConvertDelegate!!,
                processTestStepDelegate!!
            )
        }
    }
}
