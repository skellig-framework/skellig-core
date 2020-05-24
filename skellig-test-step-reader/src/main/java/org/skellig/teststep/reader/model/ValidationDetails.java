package org.skellig.teststep.reader.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class ValidationDetails {

    private String testStepId;
    private List<ValidationEntry> validationEntries;

    protected ValidationDetails(String testStepId, List<ValidationEntry> validationEntries) {
        this.testStepId = testStepId;
        this.validationEntries = validationEntries;
    }

    public Optional<String> getTestStepId() {
        return Optional.ofNullable(testStepId);
    }

    public List<ValidationEntry> getValidationEntries() {
        return validationEntries;
    }

    public static class Builder {
        private String testStepId;
        private List<ValidationEntry> validationEntries;

        public Builder() {
            validationEntries = new ArrayList<>();
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
            validationEntries.add(new ValidationEntry(validationType, actualAndExpectedValues));
            return this;
        }

        public ValidationDetails build() {
            return new ValidationDetails(testStepId, validationEntries);
        }
    }

    public static class ValidationEntry {

        private ValidationType validationType;
        private Map<String, Object> actualExpectedValues;

        public ValidationEntry(ValidationType validationType, Map<String, Object> actualExpectedValues) {
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
