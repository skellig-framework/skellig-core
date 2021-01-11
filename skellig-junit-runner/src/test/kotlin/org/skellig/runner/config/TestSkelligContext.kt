package org.skellig.runner.config

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.runner.context.SkelligTestContext

class TestSkelligContext : SkelligTestContext() {

    override val additionalTestDataConverters: List<TestDataConverter>
        get() = listOf(CustomMessageTestDataConverter())

    override val testStepProcessors: List<TestStepProcessorDetails>
        get() = listOf(
                TestStepProcessorDetails(
                        SimpleMessageTestStepProcessor.Builder()
                                .withTestScenarioState(getTestScenarioState())
                                .withValidator(getTestStepResultValidator())
                                .build(),
                        createTestStepFactoryFrom { keywordsProperties, testStepValueConverter, testDataConverter ->
                            SimpleMessageTestStepFactory(keywordsProperties, testStepValueConverter, testDataConverter)
                        }
                ),
                TestStepProcessorDetails(
                        SimpleTestStepProcessor.Builder()
                                .withTestScenarioState(getTestScenarioState())
                                .withValidator(getTestStepResultValidator())
                                .build(),
                        createTestStepFactoryFrom { keywordsProperties, testStepValueConverter, testDataConverter ->
                            SimpleTestStepFactory(keywordsProperties, testStepValueConverter, testDataConverter)
                        }
                )
        )
}