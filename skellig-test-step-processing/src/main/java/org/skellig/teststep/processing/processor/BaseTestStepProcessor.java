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


    public static abstract class Builder<T extends TestStep> {

        protected TestScenarioState testScenarioState;
        protected TestStepResultValidator validator;

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withValidator(TestStepResultValidator validator) {
            this.validator = validator;
            return this;
        }

        public abstract TestStepProcessor<T> build();
    }
}
