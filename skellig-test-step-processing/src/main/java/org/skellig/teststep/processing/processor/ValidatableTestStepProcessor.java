package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

public abstract class ValidatableTestStepProcessor<T extends TestStep> implements TestStepProcessor {

    protected TestStepResultValidator validator;

    protected ValidatableTestStepProcessor(TestStepResultValidator validator) {
        this.validator = validator;
    }

    protected void validate(TestStep testStep) {
        testStep.getValidationDetails()
                .ifPresent(validationDetails -> {
                    validationDetails.getTestStepId()
                            .ifPresent(testStepId -> {
                                validator.validate(validationDetails.getExpectedResult(), testStepId);
                            });
                });
    }
}
