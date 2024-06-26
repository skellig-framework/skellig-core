package org.skellig.teststep.processing.processor.task

import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.FunctionCallExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

internal class RunTestTaskProcessor(
    private val taskProcessor: TaskProcessor,
    private val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any?,
    private val processTestStepDelegate: (String, Map<String, Any?>) -> TestStepProcessor.TestStepRunResult
) : TaskProcessor {

    companion object {
        private val PARAMETERS = AlphanumericValueExpression("parameters")
        private val ON_PASSED = AlphanumericValueExpression("onPassed")
        private val ON_FAILED = AlphanumericValueExpression("onFailed")
    }

    private val log = logger<RunTestTaskProcessor>()

    override fun process(task: ValueExpression?, value: ValueExpression?, context: TaskProcessingContext) {
        (task as? FunctionCallExpression)?.let {
            when (value) {
                is MapValueExpression -> {
                    val testStepName = valueConvertDelegate(task.args[0], context.parameters)
                    log.info("Run test step '$testStepName")
                    val result = testStepName?.let {
                        val runTestParameters = (valueConvertDelegate(value.value[PARAMETERS], context.parameters) as? Map<String, Any?>) ?: context.parameters
                        processTestStepDelegate(testStepName.toString(), runTestParameters)
                    } ?: error("The Test Step '${task.args[0]}' was evaluated to 'null'")

                    var valueExpression: ValueExpression?
                    result.subscribe { t, _, e ->
                        valueExpression =
                            if (e == null) value.value[ON_PASSED]
                            else {
                                if (value.value.containsKey(ON_FAILED)) {
                                    t?.let { log.info(t, "Run the $ON_FAILED callback for the failed test '${t.name}'") }
                                    value.value[ON_FAILED]
                                } else throw e
                            }

                        valueExpression?.let {
                            t?.let { log.info(t, "Run the $ON_PASSED callback for the passed test '${t.name}'") }
                            taskProcessor.process(null, it, context)
                        }
                    }
                    context.addResultFromTestStep(result)
                }

                else -> error("Invalid property type of the function 'runTest'. Expected key-value pairs, found ${value?.javaClass}")
            }

        }
    }

    override fun getTaskName(): String = "runTest"

}