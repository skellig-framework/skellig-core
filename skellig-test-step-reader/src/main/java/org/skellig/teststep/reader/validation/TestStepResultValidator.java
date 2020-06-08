package org.skellig.teststep.reader.validation;

import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

public interface TestStepResultValidator {

    void validate(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult);

    boolean isApplicableFor(ValidationType validationType);
}
