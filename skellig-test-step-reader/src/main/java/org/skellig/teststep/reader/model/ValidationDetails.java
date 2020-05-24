package org.skellig.teststep.reader.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ValidationDetails {

    private String testStepId;
    private List<ExpectedTestResult> validationEntries;

    protected ValidationDetails(String testStepId, List<ExpectedTestResult> validationEntries) {
        this.testStepId = testStepId;
        this.validationEntries = validationEntries;
    }

    public Optional<String> getTestStepId() {
        return Optional.ofNullable(testStepId);
    }

    public List<ExpectedTestResult> getValidationEntries() {
        return validationEntries;
    }

    public static class Builder {
        private String testStepId;
        private List<ExpectedTestResult> expectedTestResults;

        public Builder() {
            expectedTestResults = new ArrayList<>();
        }

        public Builder withTestStepId(String testStepId) {
            this.testStepId = testStepId;
            return this;
        }

        public Builder withActualAndExpectedValues(ValidationType validationType,
                                                   String actualValue, Object expectedValue) {
            return withActualAndExpectedValues(validationType, Collections.singletonMap(actualValue, expectedValue));
        }

        public Builder withActualAndExpectedValues(ValidationType validationType,
                                                   Map<String, Object> actualAndExpectedValues) {
            expectedTestResults.add(new ExpectedTestResult(validationType, actualAndExpectedValues));
            return this;
        }

        public ValidationDetails build() {
            return new ValidationDetails(testStepId, expectedTestResults);
        }
    }

    public static class ExpectedTestResult {

        private ValidationType validationType;
        private Map<String, Object> actualExpectedValues;

        public ExpectedTestResult(ValidationType validationType, Map<String, Object> actualExpectedValues) {
            this.validationType = validationType;
            this.actualExpectedValues = actualExpectedValues;
        }

        public ValidationType getValidationType() {
            return validationType;
        }

        public Map<String, Object> getActualExpectedValues() {
            return actualExpectedValues;
        }
    }
}
