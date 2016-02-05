package org.skellig.teststep.processing.processor;

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
    public void process(TestStep testStep) {
        Objects.requireNonNull(testStep, "Test step must not be null");
        Optional<TestStepProcessor> testStepProcessor = testStepProcessors.stream()
                .filter(processor -> testStep.getClass().equals(processor.getTestStepClass()))
                .findFirst();

        if (testStepProcessor.isPresent()) {
            testStepProcessor.get().process(testStep);
        } else {
            validate(testStep, getLatestResultOfTestStep(testStep.getId()));
        }
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
