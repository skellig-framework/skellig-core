package org.skellig.teststep.processing.validation;

import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.reader.exception.ValidationException;
import org.skellig.teststep.reader.model.ValidationDetails;

public abstract class BaseTestStepResultValidator implements TestStepResultValidator {

    protected ValueComparator valueComparator;

    public BaseTestStepResultValidator(ValueComparator valueComparator) {
        this.valueComparator = valueComparator;
    }

    public BaseTestStepResultValidator() {
    }

    @Override
    public void validate(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult) {
        if (isApplicableFor(expectedResult.getValidationType())) {
            validateActualResult(actualResult, expectedResult);
        } else {
            throw new ValidationException(String.format("%s is not supported for %s",
                    expectedResult.getValidationType(), getClass().getSimpleName()));
        }
    }

    protected abstract void validateActualResult(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult);
}