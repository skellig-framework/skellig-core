package org.skellig.teststep.processing.validation;

import org.skellig.teststep.reader.model.ExpectedResult;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

public interface TestStepResultValidator {

    void validate(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult);

    boolean validate(ExpectedResult expectedResult, Object actualResult);

    boolean isApplicableFor(ValidationType validationType);
}