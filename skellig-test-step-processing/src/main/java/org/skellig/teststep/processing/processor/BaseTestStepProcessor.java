package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

public abstract class BaseTestStepProcessor<T extends TestStep> extends ValidatableTestStepProcessor<T> {

    protected BaseTestStepProcessor(TestScenarioState testScenarioState, TestStepResultValidator validator) {
        super(testScenarioState, validator);
    }

    @Override
    public void process(T testStep) {
        testScenarioState.set(testStep.getId(), testStep);

        Object result = processTestStep(testStep);

        testScenarioState.set(testStep.getId() + RESULT_SAVE_SUFFIX, result);

        validate(testStep, result);
    }

    protected abstract Object processTestStep(T testStep);
}
