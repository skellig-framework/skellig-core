package org.skellig.teststep.processing.validation;

import org.skellig.teststep.processing.exception.ValidationException;
import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.ValidationType;
import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultTestStepResultValidator implements TestStepResultValidator {

    private ValueComparator valueComparator;
    private TestStepValueExtractor valueExtractor;

    protected DefaultTestStepResultValidator(ValueComparator valueComparator,
                                             TestStepValueExtractor valueExtractor) {
        this.valueComparator = valueComparator;
        this.valueExtractor = valueExtractor;
    }

    public void validate(ExpectedResult expectedResult, Object actualResult) {
        StringBuilder errorBuilder = new StringBuilder();
        if (!validate(expectedResult, actualResult, errorBuilder)) {
            throw new ValidationException("Validation failed!\n" + errorBuilder.toString());
        }
    }

    private boolean validate(ExpectedResult expectedResult, Object actualResult, StringBuilder errorBuilder) {
        if (expectedResult.getValidationType() == ValidationType.ANY_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .anyMatch(item -> validate2(item, actualResult, errorBuilder));
        } else if (expectedResult.getValidationType() == ValidationType.ANY_NONE_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .anyMatch(item -> !validate2(item, actualResult, errorBuilder));
        } else if (expectedResult.getValidationType() == ValidationType.NONE_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .noneMatch(item -> validate2(item, actualResult, errorBuilder));
        } else if (expectedResult.getValidationType() == ValidationType.ALL_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .allMatch(item -> validate2(item, actualResult, errorBuilder));
        } else {
            boolean isValid = valueComparator.compare(expectedResult.getExpectedResult(), actualResult);

            if (expectedResult.getValidationTypeOfParent() == ValidationType.NONE_MATCH && isValid ||
                    expectedResult.getValidationTypeOfParent() != ValidationType.NONE_MATCH && !isValid) {
                constructErrorMessage(expectedResult, actualResult, errorBuilder);
            }
            return isValid;
        }
    }

    private boolean validate2(ExpectedResult expectedResult, Object actualResult, StringBuilder errorBuilder) {
        Object actualValue = extractActualValueFromExpectedResult(actualResult, expectedResult);
        if (expectedResult.getProperty() == null && actualValue instanceof Collection) {
            if (expectedResult.getValidationType() == ValidationType.NONE_MATCH) {
                return ((Collection) actualValue).stream().allMatch(actualResultItem ->
                        validate(expectedResult, actualResultItem, errorBuilder));
            } else {
                return ((Collection) actualValue).stream().anyMatch(actualResultItem ->
                        validate(expectedResult, actualResultItem, errorBuilder));
            }
        } else if (actualResult.getClass().isArray()) {
            return validate2(expectedResult, Arrays.stream((Object[]) actualResult).collect(Collectors.toList()), errorBuilder);
        } else {
            return validate(expectedResult, actualValue, errorBuilder);
        }
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
                .append(expectedResult.getValidationTypeOfParent() != ValidationType.NONE_MATCH ? "Expected: " : "Did not expect: ")
                .append(expectedResult.<Object>getExpectedResult())
                .append(" Actual: ")
                .append(actualValue)
                .append('\n');
    }

    public static class Builder {

        private ValueComparator valueComparator;
        private TestStepValueExtractor valueExtractor;

        public Builder withValueComparator(ValueComparator valueComparator) {
            this.valueComparator = valueComparator;
            return this;
        }

        public Builder withValueExtractor(TestStepValueExtractor valueExtractor) {
            this.valueExtractor = valueExtractor;
            return this;
        }

        public TestStepResultValidator build() {
            return new DefaultTestStepResultValidator(valueComparator, valueExtractor);
        }
    }
}