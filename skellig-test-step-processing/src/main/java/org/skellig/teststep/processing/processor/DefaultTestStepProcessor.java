package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.reader.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultTestStepProcessor implements TestStepProcessor<TestStep> {

    private List<TestStepProcessor> testStepProcessors;
    private TestScenarioState testScenarioState;
    private TestStepResultValidator validator;

    protected DefaultTestStepProcessor(List<TestStepProcessor> testStepProcessors,
                                       TestScenarioState testScenarioState,
                                       TestStepResultValidator validator) {
        this.testStepProcessors = testStepProcessors;
        this.testScenarioState = testScenarioState;
        this.validator = validator;
    }

    @Override
    public void process(TestStep testStep) {
        Optional<TestStepProcessor> testStepProcessor = testStepProcessors.stream()
                .filter(processor -> testStep.getClass().equals(processor.getTestStepClass()))
                .findFirst();

        testStepProcessor.ifPresent(stepProcessor -> stepProcessor.process(testStep));

        validate(testStep);
    }

    private void validate(TestStep testStep) {
        testStep.getValidationDetails()
                .ifPresent(validationDetails -> {
                    Optional<String> testStepId = validationDetails.getTestStepId();
                    if (testStepId.isPresent()) {
                        Object resultFromOtherTestStep = getResultByTestStepId(testStepId.get());
                        validator.validate(validationDetails.getExpectedResult(), resultFromOtherTestStep);
                    } else {
                        validator.validate(validationDetails.getExpectedResult(), getResultByTestStepId(testStep.getId()));
                    }
                });
    }

    private Object getResultByTestStepId(String testStepId) {
        return testScenarioState.get(testStepId + ".result")
                .orElseThrow(() -> new ValidationException(String.format("Result of the test step '%s' not found", testStepId)));
    }

    @Override
    public Class<TestStep> getTestStepClass() {
        return TestStep.class;
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
