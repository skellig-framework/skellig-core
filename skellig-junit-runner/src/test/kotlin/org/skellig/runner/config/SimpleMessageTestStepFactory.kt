package org.skellig.runner.config

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import java.util.*

class SimpleMessageTestStepFactory(
    testStepRegistry: TestStepRegistry,
    keywordsProperties: Properties?,
    testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<SimpleMessageTestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<SimpleMessageTestStep> {
        return SimpleMessageTestStep.Builder()
                .withReceiver(convertValue(rawTestStep["receiver"], parameters))
                .withReceiveFrom(convertValue(rawTestStep["receiveFrom"], parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean {
        return rawTestStep.containsKey("receiver") || rawTestStep.containsKey("receiveFrom")
    }
}