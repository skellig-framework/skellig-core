package org.skellig.teststep.processing.processor.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory

interface TestStepProcessorConfig<T : TestStep> {
    fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<T>?
}

data class TestStepProcessorConfigDetails(
    val state: TestScenarioState,
    val config: Config,
    val testStepRegistry: TestStepRegistry,
    val valueExpressionContextFactory: ValueExpressionContextFactory,
    val testStepProcessor: TestStepProcessor<TestStep>,
    val testStepFactory: TestStepFactory<TestStep>
)

data class ConfiguredTestStepProcessorDetails<T : TestStep>(
    val testStepProcessor: TestStepProcessor<T>,
    val testStepFactory: TestStepFactory<T>
)
