package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator

@DisplayName("Process test step")
class DefaultTestStepProcessorTest {

    @Test
    fun test() {
        val testStepProcessor = AProcessor()

        val stepProcessor = DefaultTestStepProcessor.Builder()
                .withTestStepProcessor(testStepProcessor)
                .withTestScenarioState(mock(TestScenarioState::class.java))
                .withValidator(mock(TestStepResultValidator::class.java))
                .build()

        stepProcessor.process(A())
    }
}
class A : TestStep(name = "name") {

}
class AProcessor : TestStepProcessor<A>{
    override fun process(testStep: A): TestStepProcessor.TestStepRunResult {
        println(testStep.name)
        return TestStepProcessor.TestStepRunResult(testStep)
    }

    override fun getTestStepClass(): Class<A> {
        return A::class.java
    }

}