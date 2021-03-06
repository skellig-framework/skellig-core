package org.skellig.runner.config

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import java.util.*

class SimpleMessageTestStepFactory(keywordsProperties: Properties?,
                                   testStepValueConverter: TestStepValueConverter?)
    : BaseDefaultTestStepFactory<SimpleMessageTestStep>(keywordsProperties, testStepValueConverter) {

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<SimpleMessageTestStep> {
        return SimpleMessageTestStep.Builder()
                .withReceiver(convertValue(rawTestStep["receiver"], parameters))
                .withReceiveFrom(convertValue(rawTestStep["receiveFrom"], parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey("receiver") || rawTestStep.containsKey("receiveFrom")
    }
}