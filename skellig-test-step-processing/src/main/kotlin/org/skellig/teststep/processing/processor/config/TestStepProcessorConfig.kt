package org.skellig.teststep.processing.processor.config

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import java.util.*

interface TestStepProcessorConfig<T : TestStep> {
    fun config(details: TestStepProcessorConfigDetails): TestStepProcessor<T>
    fun createTestStepFactory(details: TestStepProcessorConfigDetails): TestStepFactory<T>
}

data class TestStepProcessorConfigDetails(
    val validator: TestStepResultValidator,
    val state: TestScenarioState,
//    val config: Config,
    val testStepRegistry: TestStepRegistry,
    val keywordProperties: Properties?,
    val testStepFactoryValueConverter: TestStepFactoryValueConverter,
)
