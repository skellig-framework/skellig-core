package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState

internal class TaskTestStepProcessor(
    private val taskProcessor: TaskProcessor,
    testScenarioState: TestScenarioState
) : BaseTestStepProcessor<TaskTestStep>(testScenarioState) {

    override fun processTestStep(testStep: TaskTestStep): Any {
        taskProcessor.process(null, testStep.task, testStep.parameters)
        return testScenarioState
    }

    override fun getTestStepClass(): Class<TaskTestStep> = TaskTestStep::class.java
}