package org.skellig.teststep.processing.processor.task

import kotlinx.coroutines.*
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class AsyncForeachTaskProcessor(
    taskProcessor: TaskProcessor,
    valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : ForeachTaskProcessor(taskProcessor, valueConvertDelegate) {

    override fun forEach(items: Iterable<*>, itemName: String, parameters: MutableMap<String, Any?>, value: ValueExpression) {
        runBlocking {
            withContext(Dispatchers.Default) {
                forEachAsync(items, itemName, parameters, value)
            }
        }
    }

    private suspend fun forEachAsync(
        items: Iterable<*>,
        itemName: String,
        parameters: MutableMap<String, Any?>,
        value: ValueExpression
    ) {
        coroutineScope {
            items.forEach { item ->
                launch {
                    val parametersCopy = parameters.toMutableMap()
                    parametersCopy[itemName] = item
                    taskProcessor.process(null, value, parametersCopy)
                }
            }
        }
    }

    override fun getTaskName(): String = "asyncForEach"
}