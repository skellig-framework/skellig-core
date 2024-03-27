package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.reader.value.expression.FunctionCallExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal open class ForeachTaskProcessor(
    protected val taskProcessor: TaskProcessor,
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
) : TaskProcessor {

    companion object {
        private const val IT = "it"
    }

    override fun process(task: ValueExpression?, value: ValueExpression?, parameters: MutableMap<String, Any?>) {

        (task as? FunctionCallExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    if (task.args.isNotEmpty()) {
                        forEach(
                            extractItems(task, parameters)
                                ?: error("Invalid items type of the function '${getTaskName()}'. Expected array or key-value pairs, found ${value::class.java}"),
                            extractItemName(task, parameters), parameters, value
                        )
                    }
                }

                else -> error("Invalid property type of the function '${getTaskName()}'. Expected key-value pairs, found ${value?.javaClass}")
            }
        }
    }

    protected open fun forEach(
        items: Iterable<*>,
        itemName: String,
        parameters: MutableMap<String, Any?>,
        value: ValueExpression
    ) {
        items.forEach { item ->
            parameters[itemName] = item
            taskProcessor.process(null, value, parameters)
        }
    }

    private fun extractItems(task: FunctionCallExpression, parameters: MutableMap<String, Any?>): Iterable<*>? {
        return (valueConvertDelegate(task.args[0], parameters) as? Iterable<*>)
    }

    private fun extractItemName(task: FunctionCallExpression, parameters: MutableMap<String, Any?>): String {
        return if (task.args.size > 1) valueConvertDelegate(task.args[1], parameters)?.toString() ?: IT else IT
    }

    override fun getTaskName(): String = "forEach"

}