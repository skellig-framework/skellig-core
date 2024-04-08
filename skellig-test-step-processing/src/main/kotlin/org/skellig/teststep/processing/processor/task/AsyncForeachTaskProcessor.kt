package org.skellig.teststep.processing.processor.task

import kotlinx.coroutines.*
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class AsyncForeachTaskProcessor(
    taskProcessor: TaskProcessor,
    valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : ForeachTaskProcessor(taskProcessor, valueConvertDelegate) {

    override fun forEach(items: Iterable<*>, itemName: String, context: TaskProcessingContext, value: ValueExpression) {
        runBlocking {
            withContext(Dispatchers.Default) {
                forEachAsync(items, itemName, context, value)
            }
        }
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
                    taskProcessor.process(null, value, contextCopy)
                }
            }
        }
    }

    override fun getTaskName(): String = "asyncForEach"
}