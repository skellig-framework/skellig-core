package org.skellig.teststep.reader.validation;

import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

public interface TestResultValidator {

    void validate(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult);

    boolean isApplicableFor(ValidationType validationType);
}
