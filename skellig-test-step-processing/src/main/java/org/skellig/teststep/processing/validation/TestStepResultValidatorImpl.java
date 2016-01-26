package org.skellig.teststep.processing.validation;

import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.reader.model.ExpectedResult;
import org.skellig.teststep.reader.model.NewValidationType;
import org.skellig.teststep.reader.model.ValidationDetails;
import org.skellig.teststep.reader.model.ValidationType;
import org.skellig.teststep.reader.valueextractor.TestStepValueExtractor;

import java.util.List;

public class TestStepResultValidatorImpl implements TestStepResultValidator {

    private ValueComparator valueComparator;
    private TestStepValueExtractor valueExtractor;

    public boolean validate(ExpectedResult expectedResult, Object actualResult) {
        StringBuilder errorBuilder = new StringBuilder();
        return validate(expectedResult, actualResult, errorBuilder);
    }

    private boolean validate(ExpectedResult expectedResult, Object actualResult, StringBuilder errorBuilder) {

        if (expectedResult.getValidationType() == NewValidationType.ANY_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .anyMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return validate(item, actualValue, errorBuilder);
                    });
        } else if (expectedResult.getValidationType() == NewValidationType.ANY_NONE_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .anyMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return !validate(item, actualValue, errorBuilder);
                    });
        } else if (expectedResult.getValidationType() == NewValidationType.NONE_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .noneMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return validate(item, actualValue, errorBuilder);
                    });
        } else if (expectedResult.getValidationType() == NewValidationType.ALL_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .allMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return validate(item, actualValue, errorBuilder);
                    });
        } else {
            boolean isValid = valueComparator.compare(expectedResult.getExpectedResult(), actualResult);

            if (expectedResult.getValidationTypeOfParent() == NewValidationType.NONE_MATCH && isValid ||
                    expectedResult.getValidationTypeOfParent() != NewValidationType.NONE_MATCH && !isValid) {
                constructErrorMessage(expectedResult, actualResult, errorBuilder);
            }
        }
        return false;
    }

    private Object extractActualValueFromExpectedResult(Object actualResult, Object expectedResult) {
        if (ExpectedResult.class.equals(expectedResult.getClass())) {
            return valueExtractor.extract(actualResult, ((ExpectedResult) expectedResult).getProperty());
        } else {
            return actualResult;
        }
    }

    private void constructErrorMessage(ExpectedResult expectedResult, Object actualValue, StringBuilder errorBuilder) {
        errorBuilder.append(expectedResult.getFullPropertyPath())
                .append(" is not valid. ")
                .append(expectedResult.getValidationTypeOfParent() != NewValidationType.NONE_MATCH ? "Expected: " : "Did not expect: ")
                .append(expectedResult.<Object>getExpectedResult())
                .append(" Actual: ")
                .append(actualValue)
                .append('\n');
    }

    @Override
    public void validate(Object actualResult, ValidationDetails.ExpectedTestResult expectedResult) {

    }

    @Override
    public boolean isApplicableFor(ValidationType validationType) {
        return false;
    }
}