package org.skellig.teststep.processor.ui.config

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.reader.value.expression.ValueExpression

/** A third-party-style extension fixture to exercise mixed-context shutdown failure. */
class FailingShutdownConfig : TestStepProcessorConfig<DefaultTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<DefaultTestStep>? {
        if (!details.config.hasPath("shutdown-failure") || !details.config.getBoolean("shutdown-failure")) return null
        return ConfiguredTestStepProcessorDetails(
            object : TestStepProcessor<DefaultTestStep> {
                private var failOnce = true
                override fun getTestStepClass() = DefaultTestStep::class.java
                override fun process(testStep: DefaultTestStep) = TestStepProcessor.TestStepRunResult(testStep).apply { notify(null, null) }
                override fun close() {
                    if (failOnce) {
                        failOnce = false
                        error("Simulated extension shutdown failure")
                    }
                }
            },
            object : TestStepFactory<DefaultTestStep> {
                override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>) = false
                override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>) =
                    error("Shutdown-only extension has no test steps")
            }
        )
    }
}
