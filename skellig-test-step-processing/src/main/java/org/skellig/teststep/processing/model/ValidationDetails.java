package org.skellig.teststep.processing.model;

import java.util.Optional;


public class ValidationDetails {

    private String testStepId;
    private String convertTo;
    private ExpectedResult expectedResult;

    protected ValidationDetails(String testStepId, String convertTo, ExpectedResult expectedResult) {
        this.testStepId = testStepId;
        this.convertTo = convertTo;
        this.expectedResult = expectedResult;
        this.expectedResult.initializeParents();
    }

    public Optional<String> getTestStepId() {
        return Optional.ofNullable(testStepId);
    }

    public Optional<String> getConvertTo() {
        return Optional.ofNullable(convertTo);
    }

    public ExpectedResult getExpectedResult() {
        return expectedResult;
    }

    public static class Builder {
        private String testStepId;
        private String convertTo;
        private ExpectedResult expectedResult;

        public Builder withTestStepId(String testStepId) {
            this.testStepId = testStepId;
            return this;
        }

        public Builder withConvertTo(String convertTo) {
            this.convertTo = convertTo;
            return this;
        }

        public Builder withExpectedResult(ExpectedResult expectedResult) {
            this.expectedResult = expectedResult;
            return this;
        }

        public ValidationDetails build() {
            return new ValidationDetails(testStepId, convertTo, expectedResult);
        }
    }
}
