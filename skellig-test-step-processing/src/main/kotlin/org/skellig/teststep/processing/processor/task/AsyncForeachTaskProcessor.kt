package org.skellig.teststep.processing.processor.task

import kotlinx.coroutines.*
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class AsyncForeachTaskProcessor(
    taskProcessor: TaskProcessor,
    valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : ForeachTaskProcessor(taskProcessor, valueConvertDelegate) {

    private val log: Logger = LoggerFactory.getLogger(AsyncForeachTaskProcessor::class.java)

    override fun forEach(items: Iterable<*>, itemName: String, context: TaskProcessingContext, value: ValueExpression) {
        log.info("Run the task '${getTaskName()}' asynchronously each iteration of sub-tasks and wait until finished")
        runBlocking {
            withContext(Dispatchers.Default) {
                forEachAsync(items, itemName, context, value)
            }
        }
        log.info("The task '${getTaskName()}' has been finished")
    }

    private suspend fun forEachAsync(
        items: Iterable<*>,
        itemName: String,
        context: TaskProcessingContext,
        value: ValueExpression
    ) {
        coroutineScope {
            items.forEach { item ->
                launch {
                    val contextCopy = TaskProcessingContext(context)
                    contextCopy.parameters[itemName] = item
                    log.debug("Run iteration of the task '${getTaskName()}' with $itemName = $item")
                    taskProcessor.process(null, value, contextCopy)
                    log.debug("Iteration $itemName with value $item of the task '${getTaskName()}' has finished")
                }
            }
        }
    }

    override fun getTaskName(): String = "asyncForEach"
}