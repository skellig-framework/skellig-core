package org.skellig.teststep.reader.model;

import java.util.Optional;

public class TestStep {

    private String id;
    private String name;
    private Object testData;
    private ValidationDetails validationDetails;

    protected TestStep(String id, String name, Object testData, ValidationDetails validationDetails) {
        this.id = id;
        this.name = name;
        this.testData = testData;
        this.validationDetails = validationDetails;
    }

    public String getId() {
        return id == null ? name : id;
    }

    public String getName() {
        return name;
    }

    public Object getTestData() {
        return testData;
    }

    public Optional<ValidationDetails> getValidationDetails() {
        return Optional.ofNullable(validationDetails);
    }

    public static class Builder {
        protected String id;
        protected String name;
        protected Object testData;
        protected ValidationDetails validationDetails;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTestData(Object testData) {
            this.testData = testData;
            return this;
        }

        public Builder withValidationDetails(ValidationDetails validationDetails) {
            this.validationDetails = validationDetails;
            return this;
        }

        public TestStep build() {
            return new TestStep(id, name, testData, validationDetails);
        }
    }
}