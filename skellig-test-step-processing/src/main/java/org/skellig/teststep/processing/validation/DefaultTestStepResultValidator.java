package org.skellig.teststep.processing.validation;


import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.reader.exception.ValidationException;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;

import java.util.List;

public class DefaultTestStepResultValidator extends BaseTestStepResultValidator {

    public DefaultTestStepResultValidator(ValueComparator valueComparator) {
        super(valueComparator);
    }

    @Override
    protected void validateActualResult(Object actualResult, ValidationDetails.ExpectedTestResult expectedTestResult) {
        expectedTestResult.getActualExpectedValues()
                .forEach((key, expectedValues) -> {
                    if (expectedValues instanceof List) {
                        ((List) expectedValues).forEach(expectedValue -> compareActualAndExpectedValue(actualResult, expectedValue));
                    } else {
                        compareActualAndExpectedValue(actualResult, expectedValues);
                    }
                });
    }

    private void compareActualAndExpectedValue(Object actualValue, Object expectedValue) {
        if (!valueComparator.compare(actualValue, expectedValue)) {
            String errorMessage =
                    String.format("Failed validation of test result. Expected: '%s', actual: '%s'",
                            expectedValue, actualValue);
            throw new ValidationException(errorMessage);
        }
    }

    @Override
    public boolean isApplicableFor(ValidationType validationType) {
        return ValidationType.DEFAULT.equals(validationType);
    }
}
