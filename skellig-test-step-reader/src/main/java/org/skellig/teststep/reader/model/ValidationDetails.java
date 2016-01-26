package org.skellig.teststep.reader.model;

import java.util.Optional;


public class ValidationDetails {

    private String testStepId;
    private ExpectedResult expectedResult;

    protected ValidationDetails(String testStepId, ExpectedResult expectedResult) {
        this.testStepId = testStepId;
        this.expectedResult = expectedResult;
    }

    public Optional<String> getTestStepId() {
        return Optional.ofNullable(testStepId);
    }

    public ExpectedResult getExpectedResult() {
        return expectedResult;
    }

    public static class Builder {
        private String testStepId;
        private ExpectedResult expectedResult;

        public Builder withTestStepId(String testStepId) {
            this.testStepId = testStepId;
            return this;
        }

        public Builder withExpectedResult(ExpectedResult expectedResult) {
            this.expectedResult = expectedResult;
            return this;
        }

        public ValidationDetails build() {
            return new ValidationDetails(testStepId, expectedResult);
        }
    }
}
