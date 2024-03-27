package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.FunctionCallExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class RunTestTaskProcessor(
    private val taskProcessor: TaskProcessor,
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
    private val runTestDelegate: (String, Map<String, Any?>?) -> TestStepProcessor.TestStepRunResult
) : TaskProcessor {

    override fun process(task: ValueExpression?, value: ValueExpression?, parameters: MutableMap<String, Any?>) {
        (task as? FunctionCallExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    val runTestParameters = (valueConvertDelegate(value.value[AlphanumericValueExpression("parameters")], parameters) as? Map<String, Any?>)

                    val result = valueConvertDelegate(task.args[0], parameters)?.let { testName ->
                        runTestDelegate(testName.toString(), runTestParameters)
                    }
                    result?.subscribe { _, _, e ->
                        if (e == null) {
                            value.value[AlphanumericValueExpression("onPassed")]
                                ?.let { onPassed -> taskProcessor.process(null, onPassed, parameters) }
                        } else {
                            value.value[AlphanumericValueExpression("onFailed")]
                                ?.let { onFailed -> taskProcessor.process(null, onFailed, parameters) }
                        }
                    }
                }

                else -> error("Invalid property type of the function 'runTest'. Expected key-value pairs, found ${value?.javaClass}")
            }

        }
    }

    override fun getTaskName(): String = "runTest"

}