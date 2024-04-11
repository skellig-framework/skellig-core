package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger

internal class TaskTestStepProcessor(
    private val taskProcessor: TaskProcessor,
    testScenarioState: TestScenarioState
) : BaseTestStepProcessor<TaskTestStep>(testScenarioState) {

    private val log = logger<TaskTestStepProcessor>()

    override fun processTestStep(testStep: TaskTestStep): Any {
        TaskProcessingContext(testStep.parameters).use { context ->
            log.info(testStep, "Start to process task of test '${testStep.name}'")
            try {
                taskProcessor.process(null, testStep.getTask(), context)
            } finally {
                log.info(testStep,
                    "Processing of test '${testStep.name}' has finished. " +
                            "Start to wait for unfinished results of test steps if any remaining"
                )
                context.getTestStepsResult().forEach { it.awaitResult() }
            }
        }
        return testScenarioState
    }

    override fun getTestStepClass(): Class<TaskTestStep> = TaskTestStep::class.java
}