package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultTestStepProcessor extends ValidatableTestStepProcessor<TestStep> {

    private List<TestStepProcessor> testStepProcessors;

    protected DefaultTestStepProcessor(List<TestStepProcessor> testStepProcessors,
                                       TestScenarioState testScenarioState,
                                       TestStepResultValidator validator) {
        super(testScenarioState, validator);
        this.testStepProcessors = testStepProcessors;
    }

    @Override
    public TestStepRunResult process(TestStep testStep) {
        Objects.requireNonNull(testStep, "Test step must not be null");
        Optional<TestStepProcessor> testStepProcessor = testStepProcessors.stream()
                .filter(processor -> testStep.getClass().equals(processor.getTestStepClass()))
                .findFirst();

        if (testStepProcessor.isPresent()) {
            return testStepProcessor.get().process(testStep);
        } else {
            TestStepRunResult testStepRunResult = new TestStepRunResult(testStep);
            testScenarioState.set(testStep.getId(), testStep);
            validate(testStep, testStepRunResult);
            return testStepRunResult;
        }
    }

    private void validate(TestStep testStep, TestStepRunResult testStepRunResult) {
        RuntimeException error = null;
        try {
            if (testStep.getValidationDetails().isPresent()) {
                super.validate(testStep);
            }
        } catch (ValidationException ex) {
            error = ex;
        } finally {
            testStepRunResult.notify(null, error);
        }
    }

    @Override
    public Class<TestStep> getTestStepClass() {
        return TestStep.class;
    }

    @Override
    public void close() {
        testStepProcessors.forEach(TestStepProcessor::close);
    }

    public static class Builder {

        private List<TestStepProcessor> testStepProcessors;
        private TestScenarioState testScenarioState;
        private TestStepResultValidator validator;

        public Builder() {
            testStepProcessors = new ArrayList<>();
        }

        public Builder withTestStepProcessor(TestStepProcessor<? extends TestStep> testStepProcessor) {
            this.testStepProcessors.add(testStepProcessor);
            return this;
        }

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withValidator(TestStepResultValidator validator) {
            this.validator = validator;
            return this;
        }

        public TestStepProcessor<TestStep> build() {
            Objects.requireNonNull(testScenarioState, "TestScenarioState must be provided");

            return new DefaultTestStepProcessor(testStepProcessors, testScenarioState, validator);
        }
    }
}
