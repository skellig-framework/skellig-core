package org.skellig.teststep.processing.processor.task

import kotlinx.coroutines.*
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class AsyncEachTaskProcessor(
    private val taskProcessor: TaskProcessor,
) : TaskProcessor {

    private val log = logger<AsyncEachTaskProcessor>()

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        (task as? AlphanumericValueExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    log.info("Run the task '${getTaskName()}' asynchronously for each sub-task and wait until finished")
                    runBlocking {
                        withContext(Dispatchers.Default) {
                            forEachAsync(context, value)
                        }
                    }
                    log.info("The task '${getTaskName()}' has been finished")
                }

                else -> error("Invalid property type of the function 'asyncEach'. Expected key-value pairs, found ${value?.javaClass}")
            }
        }
    }

    private suspend fun forEachAsync(context: TaskProcessingContext, value: MapValueExpression) {
        coroutineScope {
            value.value.forEach { item ->
                launch {
                    item.value?.let { taskProcessor.process(item.key, it, context) }
                }
            }
        }
    }

    override fun getTaskName(): String = "asyncEach"

}