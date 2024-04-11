package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.FunctionCallExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class DefaultTaskProcessor(
    testScenarioState: TestScenarioState,
    valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
    processTestStepDelegate: (String, Map<String, Any?>) -> TestStepProcessor.TestStepRunResult
) : TaskProcessor {

    private val log = logger<DefaultTaskProcessor>()
    private val tasks = mutableMapOf<String, TaskProcessor>()

    init {
        registerTask(VariableTaskProcessor(valueConvertDelegate))
        registerTask(ForeachTaskProcessor(this, valueConvertDelegate))
        registerTask(AsyncForeachTaskProcessor(this, valueConvertDelegate))
        registerTask(RunTestTaskProcessor(this, valueConvertDelegate, processTestStepDelegate))
        registerTask(AsyncEachTaskProcessor(this))
        registerTask(StateTaskProcessor(testScenarioState, valueConvertDelegate))
        registerTask(RunIfTaskProcessor(this, valueConvertDelegate))
    }

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        val taskName = getTaskName(task)
        taskName?.let {
            getTaskProcessor(taskName).process(task, value, context)
        } ?: if (value is MapValueExpression) {
            value.value.forEach { t ->
                val innerTaskName = getTaskName(t.key) ?: ""
                getTaskProcessor(innerTaskName).process(t.key, t.value, context)
            }
        } else if (task != null) {
            getTaskProcessor().process(task, value, context)
        } else error("Key must not be null or Value type must be MapValueExpression")
    }

    private fun getTaskProcessor(taskName: String = ""): TaskProcessor = tasks[taskName] ?: tasks[""]!!

    private fun getTaskName(task: ValueExpression?): String? {
        val taskName = when (task) {
            is FunctionCallExpression -> task.name
            is AlphanumericValueExpression -> task.toString()
            else -> null
        }
        return taskName
    }

    private fun registerTask(taskProcessor: TaskProcessor) {
        tasks[taskProcessor.getTaskName()] = taskProcessor
        log.debug {"Register processor '${taskProcessor::class.java}' for the task '${taskProcessor.getTaskName()}'"}
    }

    override fun getTaskName(): String = ""

}