package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.processor.CompositeTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class TaskTestStepProcessor(private val testStepProcessor: CompositeTestStepProcessor) : TestStepProcessor<TaskTestStep> {

    private val taskProcessors = mutableListOf<TaskProcessor>()

    override fun process(testStep: TaskTestStep): TestStepProcessor.TestStepRunResult {
        processInternal(testStep.task)

        val testStepResult = TestStepProcessor.TestStepRunResult(testStep)

        return testStepResult
    }

    private fun processInternal(task: ValueExpression?) {
        task?.let {
//            taskProcessors.forEach { it.process(task) }
        }
    }

    override fun getTestStepClass(): Class<TaskTestStep> = TaskTestStep::class.java
}