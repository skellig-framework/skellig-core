package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

import java.util.Optional;

import static org.skellig.task.TaskUtils.runTask;

public abstract class ValidatableTestStepProcessor<T extends TestStep> implements TestStepProcessor<T> {

    protected static final String RESULT_SAVE_SUFFIX = ".result";

    protected final TestScenarioState testScenarioState;
    protected final TestStepResultValidator validator;
    protected final TestStepResultConverter testStepResultConverter;

    protected ValidatableTestStepProcessor(TestScenarioState testScenarioState, TestStepResultValidator validator,
                                           TestStepResultConverter testStepResultConverter) {
        this.testScenarioState = testScenarioState;
        this.validator = validator;
        this.testStepResultConverter = testStepResultConverter;
    }

    protected void validate(TestStep testStep) {
        validate(testStep, null);
    }

    protected void validate(TestStep testStep, Object actualResult) {
        testStep.getValidationDetails()
                .ifPresent(validationDetails -> {
                    Optional<String> testStepId = validationDetails.getTestStepId();
                    if (testStepId.isPresent()) {
                        Optional<Object> actualResultFromAnotherStep =
                                getLatestResultOfTestStep(testStepId.get(), testStep.getDelay(), testStep.getTimeout());
                        if (actualResultFromAnotherStep.isPresent()) {
                            validate(testStep.getId(), validationDetails, actualResultFromAnotherStep.get());
                        } else {
                            throw new ValidationException(String.format("Result from test step with id '%s' was not found " +
                                    "in Test Scenario State", testStepId.get()));
                        }
                    } else {
                        validate(testStep.getId(), validationDetails, actualResult);
                    }
                });
    }

    private void validate(String testStepId, ValidationDetails validationDetails, Object actualResult) {
        try {
            Optional<String> convertTo = validationDetails.getConvertTo();
            if (convertTo.isPresent()) {
                actualResult = testStepResultConverter.convert(convertTo.get(), actualResult);
            }
            validator.validate(validationDetails.getExpectedResult(), actualResult);
        } catch (ValidationException ex) {
            throw new ValidationException(ex.getMessage(), testStepId);
        }
    }

    private Optional<Object> getLatestResultOfTestStep(String testStepId, int delay, int timeout) {
        return runTask(() -> testScenarioState.get(testStepId + RESULT_SAVE_SUFFIX), Optional::isPresent, delay, timeout);
    }
}
