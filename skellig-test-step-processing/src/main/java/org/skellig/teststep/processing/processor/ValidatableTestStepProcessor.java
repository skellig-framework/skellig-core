package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.reader.exception.ValidationException;

import java.util.Optional;

public abstract class ValidatableTestStepProcessor<T extends TestStep> implements TestStepProcessor<T> {

    protected static final String RESULT_SAVE_SUFFIX = ".result";

    protected final TestScenarioState testScenarioState;
    protected final TestStepResultValidator validator;

    protected ValidatableTestStepProcessor(TestScenarioState testScenarioState, TestStepResultValidator validator) {
        this.testScenarioState = testScenarioState;
        this.validator = validator;
    }

    protected void validate(TestStep testStep, Object actualResult) {
        testStep.getValidationDetails()
                .ifPresent(validationDetails -> {
                    Optional<String> testStepId = validationDetails.getTestStepId();
                    if (testStepId.isPresent()) {
                        Optional<Object> actualResultFromAnotherStep = getLatestResultOfTestStep(testStepId.get());
                        if (actualResultFromAnotherStep.isPresent()) {
                            validator.validate(validationDetails.getExpectedResult(), actualResultFromAnotherStep.get());
                        } else {
                            throw new ValidationException(String.format("Result from test step with id '%s' was not found " +
                                    "in Test Scenario State", testStepId.get()));
                        }
                    } else {
                        validator.validate(validationDetails.getExpectedResult(), actualResult);
                    }
                });
    }

    protected Optional<Object> getLatestResultOfTestStep(String testStepId) {
        return testScenarioState.get(testStepId + RESULT_SAVE_SUFFIX);
    }
}
