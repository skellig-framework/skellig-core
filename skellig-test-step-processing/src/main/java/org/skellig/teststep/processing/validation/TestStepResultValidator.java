package org.skellig.teststep.processing.validation;

import org.skellig.teststep.reader.model.ExpectedResult;

public interface TestStepResultValidator {

    boolean validate(ExpectedResult expectedResult, Object actualResult);
}