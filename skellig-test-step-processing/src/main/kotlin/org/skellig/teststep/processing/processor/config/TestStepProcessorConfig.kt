package org.skellig.teststep.processing.processor.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory

/**
 * Interface for configuring a test step processor.
 * Implementations of this interface should provide a way to configure a test step processor
 * with the given test step processor configuration details.
 * All implementations of this interface are automatically scanned by [SkelligContext][org.skellig.teststep.runner.context.SkelligTestContext],
 * as long as the property 'packageToScan' is set in the Skellig [Config] file.
 *
 * @param T the type of test step being configured.
 */
interface TestStepProcessorConfig<T : TestStep> {
    fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<T>?
}

/**
 * Represents the configuration details required for configuring a Test Step Processor.
 *
 * @property state The test scenario state that holds information about running test steps.
 * @property config The configuration object for the test step.
 * @property testStepRegistry The registry for storing and retrieving test steps.
 * @property valueExpressionContextFactory The factory for creating value expression contexts.
 * @property testStepProcessor The processor for processing the test step.
 * @property testStepFactory The factory for creating test steps.
 */
data class TestStepProcessorConfigDetails(
    val state: TestScenarioState,
    val config: Config,
    val testStepRegistry: TestStepRegistry,
    val valueExpressionContextFactory: ValueExpressionContextFactory,
    val testStepProcessor: TestStepProcessor<TestStep>,
    val testStepFactory: TestStepFactory<TestStep>
)

/**
 * Represents the details of a configured Test Step Processor.
 * It contains the Test Step Processor and the Test Step Factory associated with it.
 *
 * @param T the type of test step
 * @property testStepProcessor the test step processor responsible for processing the test step
 * @property testStepFactory the test step factory responsible for creating a Test Step instance from a raw test step data
 */
data class ConfiguredTestStepProcessorDetails<T : TestStep>(
    val testStepProcessor: TestStepProcessor<T>,
    val testStepFactory: TestStepFactory<T>
)
