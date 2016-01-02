package org.skellig.teststep.reader.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ValidationDetails {

    private String testStepId;
    private Map<ValidationType, Map<String, Object>> actualAndExpectedValues;

    protected ValidationDetails(String testStepId, Map<ValidationType, Map<String, Object>> actualAndExpectedValues) {
        this.testStepId = testStepId;
        this.actualAndExpectedValues = actualAndExpectedValues;
    }

    public Optional<String> getTestStepId() {
        return Optional.ofNullable(testStepId);
    }

    public Map<ValidationType, Map<String, Object>> getActualAndExpectedValues() {
        return actualAndExpectedValues;
    }

    public static class Builder {
        private String testStepId;
        private Map<ValidationType, Map<String, Object>> actualAndExpectedValues;

        public Builder() {
            actualAndExpectedValues = new HashMap<>();
        }

        public Builder withTestStepId(String testStepId) {
            this.testStepId = testStepId;
            return this;
        }

        public Builder withActualAndExpectedValues(ValidationType validationType,
                                                   Map<String, Object> actualAndExpectedValues) {
            this.actualAndExpectedValues.put(validationType, actualAndExpectedValues);
            return this;
        }

        public ValidationDetails build(){
            return new ValidationDetails(testStepId, actualAndExpectedValues);
        }
    }
}
