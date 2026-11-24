package org.skellig.teststep.processor.scopewarning

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.config.*
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.reader.value.expression.ValueExpression

class ScopeWarningProcessorConfig : TestStepProcessorConfig<ScopeWarningStep> {
    override fun config(details: TestStepProcessorConfigDetails) = ConfiguredTestStepProcessorDetails(
        ScopeWarningProcessor(),
        object : TestStepFactory<ScopeWarningStep> {
            override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>) =
                rawTestStep.keys.any { it.toString() == "scopeWarningFixture" }

            override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>) =
                ScopeWarningStep(testStepName, parameters[TestScenarioScope.PARAMETER_KEY] as TestScenarioScope)
        }
    )
}

class ScopeWarningStep(override val name: String, override val scope: TestScenarioScope) : TestStep

class ScopeWarningProcessor : TestStepProcessor<ScopeWarningStep> {
    override fun getTestStepClass() = ScopeWarningStep::class.java
    override fun process(testStep: ScopeWarningStep) = TestStepProcessor.TestStepRunResult(testStep).apply { notify("done", null) }
}
