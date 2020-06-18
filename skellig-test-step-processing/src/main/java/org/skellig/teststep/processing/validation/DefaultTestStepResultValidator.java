package org.skellig.teststep.processing.validation;

import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.ValidationType;
import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.List;

public class DefaultTestStepResultValidator implements TestStepResultValidator {

    private ValueComparator valueComparator;
    private TestStepValueExtractor valueExtractor;

    protected DefaultTestStepResultValidator(ValueComparator valueComparator,
                                             TestStepValueExtractor valueExtractor) {
        this.valueComparator = valueComparator;
        this.valueExtractor = valueExtractor;
    }

    public boolean validate(ExpectedResult expectedResult, Object actualResult) {
        StringBuilder errorBuilder = new StringBuilder();
        return validate(expectedResult, actualResult, errorBuilder);
    }

    private boolean validate(ExpectedResult expectedResult, Object actualResult, StringBuilder errorBuilder) {

        if (expectedResult.getValidationType() == ValidationType.ANY_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .anyMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return validate(item, actualValue, errorBuilder);
                    });
        } else if (expectedResult.getValidationType() == ValidationType.ANY_NONE_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .anyMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return !validate(item, actualValue, errorBuilder);
                    });
        } else if (expectedResult.getValidationType() == ValidationType.NONE_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .noneMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return validate(item, actualValue, errorBuilder);
                    });
        } else if (expectedResult.getValidationType() == ValidationType.ALL_MATCH) {
            return expectedResult.<List<ExpectedResult>>getExpectedResult().stream()
                    .allMatch(item -> {
                        Object actualValue = extractActualValueFromExpectedResult(actualResult, item);
                        return validate(item, actualValue, errorBuilder);
                    });
        } else {
            boolean isValid = valueComparator.compare(expectedResult.getExpectedResult(), actualResult);

            if (expectedResult.getValidationTypeOfParent() == ValidationType.NONE_MATCH && isValid ||
                    expectedResult.getValidationTypeOfParent() != ValidationType.NONE_MATCH && !isValid) {
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