package org.skellig.runner.config

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import java.util.*

class SimpleTestStepFactory(keywordsProperties: Properties?,
                            testStepValueConverter: TestStepValueConverter?,
                            testDataConverter: TestDataConverter?)
    : BaseDefaultTestStepFactory<SimpleTestStepFactory.SimpleTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey("captureData")
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<SimpleTestStep> {
        return SimpleTestStep.Builder()
                .withCaptureData(convertValue<String>(rawTestStep["captureData"], parameters))
    }

    class SimpleTestStep(id: String,
                         override val name: String,
                         val captureData: String) : DefaultTestStep(id = id, name = name) {

        class Builder : DefaultTestStep.Builder<SimpleTestStep>() {

            private var captureData: String? = null

            fun withCaptureData(captureData: String?) = apply {
                this.captureData = captureData
            }

            override fun build(): SimpleTestStep {
                return SimpleTestStep(id = id!!, name = name!!, captureData = captureData!!)
            }
        }
    }
}