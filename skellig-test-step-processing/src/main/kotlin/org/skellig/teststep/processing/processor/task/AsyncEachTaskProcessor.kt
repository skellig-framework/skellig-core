package org.skellig.teststep.processing.processor.task

import kotlinx.coroutines.*
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class AsyncEachTaskProcessor(
    private val taskProcessor: TaskProcessor,
) : TaskProcessor {

    override fun process(task: ValueExpression?, value: ValueExpression?, parameters: MutableMap<String, Any?>) {
        (task as? AlphanumericValueExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    runBlocking {
                        withContext(Dispatchers.Default) {
                            forEachAsync(parameters, value)
                        }
                    }
                }

                else -> error("Invalid property type of the function 'asyncEach'. Expected key-value pairs, found ${value?.javaClass}")
            }

        }
    }

    private suspend fun forEachAsync(parameters: MutableMap<String, Any?>, value: MapValueExpression) {
        coroutineScope {
            value.value.forEach { item ->
                launch {
                    item.value?.let { taskProcessor.process(item.key, it, parameters) }
                }
            }
        }
    }

    override fun getTaskName(): String = "asyncEach"

}