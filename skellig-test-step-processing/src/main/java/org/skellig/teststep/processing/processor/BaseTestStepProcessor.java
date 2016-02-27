package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

import static org.skellig.task.async.AsyncTaskUtils.runTaskAsync;

public abstract class BaseTestStepProcessor<T extends TestStep> extends ValidatableTestStepProcessor<T> {

    protected BaseTestStepProcessor(TestScenarioState testScenarioState, TestStepResultValidator validator,
                                    TestStepResultConverter testStepResultConverter) {
        super(testScenarioState, validator, testStepResultConverter);
    }

    @Override
    public TestStepRunResult process(T testStep) {
        TestStepRunResult testStepRunResult = new TestStepRunResult(testStep);
        testScenarioState.set(testStep.getId(), testStep);

        if (testStep.getExecution() == TestStepExecutionType.ASYNC) {
            runTaskAsync(() -> processAndValidate(testStep, testStepRunResult));
        } else {
            processAndValidate(testStep, testStepRunResult);
        }
        return testStepRunResult;
    }

    protected abstract Object processTestStep(T testStep);

    private void processAndValidate(T testStep, TestStepRunResult testStepRunResult) {
        Object result = null;
        RuntimeException error = null;
        try {
            result = processTestStep(testStep);

            testScenarioState.set(testStep.getId() + RESULT_SAVE_SUFFIX, result);

            validate(testStep, result);
        } catch (ValidationException | TestStepProcessingException ex) {
            error = ex;
        } catch (Throwable ex) {
            error = new TestStepProcessingException(ex.getMessage(), ex);
        } finally {
            testStepRunResult.notify(result, error);
        }
    }


    public static abstract class Builder<T extends TestStep> {

        protected TestScenarioState testScenarioState;
        protected TestStepResultValidator validator;
        protected TestStepResultConverter testStepResultConverter;

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withValidator(TestStepResultValidator validator) {
            this.validator = validator;
            return this;
        }

        public Builder withTestStepResultConverter(TestStepResultConverter testStepResultConverter) {
            this.testStepResultConverter = testStepResultConverter;
            return this;
        }

        public abstract TestStepProcessor<T> build();
    }
}
