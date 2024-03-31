package org.skellig.teststep.processing.processor.task.config

import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.model.factory.TaskTestStepFactory
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processing.processor.task.DefaultTaskProcessor
import org.skellig.teststep.processing.processor.task.TaskTestStepProcessor
import org.skellig.teststep.reader.value.expression.ValueExpression

class TaskTestStepProcessorConfig : TestStepProcessorConfig<TaskTestStep> {

    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<TaskTestStep>? {

        val valueConvertDelegate: (ValueExpression?, Map<String, Any?>) -> Any? = { v, p ->
            v?.evaluate(details.valueExpressionContextFactory.create(p))
        }

        val processTestStepDelegate: (String, Map<String, Any?>) -> TestStepProcessor.TestStepRunResult = { name, parameters ->
            val rawTestStepToRun = details.testStepRegistry.getByName(name)
                ?: error("Test step '$name' is not found in any of test data files or classes")
            val testStep = details.testStepFactory.create(name, rawTestStepToRun, parameters)
            details.testStepProcessor.process(testStep)
        }

        return ConfiguredTestStepProcessorDetails(
            TaskTestStepProcessor(
                DefaultTaskProcessor(
                    details.state,
                    valueConvertDelegate,
                    processTestStepDelegate
                ),
                details.state
            ),
            TaskTestStepFactory(
                details.testStepRegistry,
                details.valueExpressionContextFactory
            )
        )
    }
}