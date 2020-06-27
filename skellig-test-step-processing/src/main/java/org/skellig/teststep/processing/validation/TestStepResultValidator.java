package org.skellig.teststep.processing.validation;

import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.ExpectedResult;

public interface TestStepResultValidator {

    void validate(ExpectedResult expectedResult, Object actualResult) throws ValidationException;
}