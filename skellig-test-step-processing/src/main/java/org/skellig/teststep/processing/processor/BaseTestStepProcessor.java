package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.state.TestScenarioState;

public abstract class BaseTestStepProcessor<T extends TestStep> implements TestStepProcessor<T> {

    private TestScenarioState testScenarioState;

    protected BaseTestStepProcessor(TestScenarioState testScenarioState) {
        this.testScenarioState = testScenarioState;
    }

    @Override
    public void process(T testStep) {
        testScenarioState.set(testStep.getId(), testStep);

        Object result = processTestStep(testStep);

        testScenarioState.set(testStep.getId() + ".result", result);
    }

    protected abstract Object processTestStep(T testStep);

}
