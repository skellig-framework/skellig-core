package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class TaskTestStepProcessor(
    private val taskProcessor: TaskProcessor,
    testScenarioState: TestScenarioState
) : BaseTestStepProcessor<TaskTestStep>(testScenarioState) {

    private val log: Logger = LoggerFactory.getLogger(TaskTestStepProcessor::class.java)

    override fun processTestStep(testStep: TaskTestStep): Any {
        TaskProcessingContext(testStep.parameters).use { context ->
            log.info("[${testStep.hashCode()}]: Start to process task of test '${testStep.name}'")
            try {
                taskProcessor.process(null, testStep.getTask(), context)
            } finally {
                log.info("[${testStep.hashCode()}]: Processing of test '${testStep.name}' has finished. " +
                        "Start to wait for unfinished results of test steps if any remaining")
                context.getTestStepsResult().forEach { it.awaitResult() }
            }
        }
        return testScenarioState
    }

    override fun getTestStepClass(): Class<TaskTestStep> = TaskTestStep::class.java
}