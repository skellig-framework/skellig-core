package org.skellig.teststep.processing.validation;

import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.reader.exception.ValidationException;

public interface TestStepResultValidator {

    void validate(ExpectedResult expectedResult, Object actualResult) throws ValidationException;
}