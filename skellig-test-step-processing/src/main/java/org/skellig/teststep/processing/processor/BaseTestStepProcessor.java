package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

import static org.skellig.task.async.AsyncTaskUtils.runTaskAsync;

public abstract class BaseTestStepProcessor<T extends TestStep> extends ValidatableTestStepProcessor<T> {

    protected BaseTestStepProcessor(TestScenarioState testScenarioState, TestStepResultValidator validator) {
        super(testScenarioState, validator);
    }

    @Override
    public void process(T testStep) {
        testScenarioState.set(testStep.getId(), testStep);

        if (testStep.getExecution() == TestStepExecutionType.ASYNC) {
            runTaskAsync(() -> processAndValidate(testStep));
        } else {
            processAndValidate(testStep);
        }
    }

    protected abstract Object processTestStep(T testStep);

    private void processAndValidate(T testStep) {
        Object result = processTestStep(testStep);

        testScenarioState.set(testStep.getId() + RESULT_SAVE_SUFFIX, result);

        validate(testStep, result);
    }


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
