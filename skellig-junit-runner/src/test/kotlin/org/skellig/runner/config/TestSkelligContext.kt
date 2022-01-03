package org.skellig.runner.config

import org.skellig.teststep.runner.context.SkelligTestContext

class TestSkelligContext : SkelligTestContext() {

    override val testStepProcessors: List<TestStepProcessorDetails>
        get() = listOf(
            createTestStepProcessorFrom(
                    SimpleMessageTestStepProcessor.Builder()
                        .withTestScenarioState(getTestScenarioState())
                        .withValidator(getTestStepResultValidator())
                        .build()
            ) { testStepRegistry, props, converter -> SimpleMessageTestStepFactory(testStepRegistry, props, converter) },
            createTestStepProcessorFrom(
                    SimpleTestStepProcessor.Builder()
                        .withTestScenarioState(getTestScenarioState())
                        .withValidator(getTestStepResultValidator())
                        .build()
            ) { testStepRegistry, props, converter -> SimpleTestStepFactory(testStepRegistry, props, converter) }
        )
}