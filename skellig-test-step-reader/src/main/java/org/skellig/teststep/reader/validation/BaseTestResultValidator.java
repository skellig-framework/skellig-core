package org.skellig.teststep.reader.validation;

import org.skellig.teststep.reader.exception.ValidationException;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.validation.comparator.ValueComparator;

public abstract class BaseTestResultValidator implements TestResultValidator {

    protected ValueComparator valueComparator;

    public BaseTestResultValidator(ValueComparator valueComparator) {
        this.valueComparator = valueComparator;
    }

    public BaseTestResultValidator() {
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