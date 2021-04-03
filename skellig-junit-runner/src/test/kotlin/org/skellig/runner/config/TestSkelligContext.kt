package org.skellig.runner.config

import org.skellig.teststep.runner.context.SkelligTestContext

class TestSkelligContext : SkelligTestContext() {

    override val testStepProcessors: List<TestStepProcessorDetails>
        get() = listOf(
                TestStepProcessorDetails(
                        SimpleMessageTestStepProcessor.Builder()
                                .withTestScenarioState(getTestScenarioState())
                                .withValidator(getTestStepResultValidator())
                                .build(),
                        createTestStepFactoryFrom { keywordsProperties, testStepValueConverter ->
                            SimpleMessageTestStepFactory(keywordsProperties, testStepValueConverter)
                        }
                ),
                TestStepProcessorDetails(
                        SimpleTestStepProcessor.Builder()
                                .withTestScenarioState(getTestScenarioState())
                                .withValidator(getTestStepResultValidator())
                                .build(),
                        createTestStepFactoryFrom { keywordsProperties, testStepValueConverter ->
                            SimpleTestStepFactory(keywordsProperties, testStepValueConverter)
                        }
                )
        )
}