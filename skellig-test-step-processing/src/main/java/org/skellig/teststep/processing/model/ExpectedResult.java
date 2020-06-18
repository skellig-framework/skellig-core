package org.skellig.teststep.processing.model;

import java.util.List;

public class ExpectedResult {

    private String property;
    private Object expectedResult;
    private ValidationType validationType;
    private ExpectedResult parent;

    public ExpectedResult(String property, Object expectedResult, ValidationType validationType) {
        this.property = property;
        this.expectedResult = expectedResult;
        this.validationType = validationType;
    }

    public String getProperty() {
        return property;
    }

    public <T> T getExpectedResult() {
        return (T) expectedResult;
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    public ValidationType getValidationTypeOfParent() {
        return parent != null ? parent.getValidationType() : ValidationType.ALL_MATCH;
    }

    public String getFullPropertyPath() {
        StringBuilder pathBuilder = new StringBuilder();
        constructFullPropertyPath(parent, pathBuilder);
        return pathBuilder.toString();
    }

    void initializeParents() {
        if (expectedResult instanceof List) {
            this.<List<ExpectedResult>>getExpectedResult()
                    .forEach(expectedResult -> {
                        expectedResult.parent = this;
                        expectedResult.initializeParents();
                    });
        }
    }

    private void constructFullPropertyPath(ExpectedResult parent, StringBuilder pathBuilder) {
        if (parent.parent != null) {
            constructFullPropertyPath(parent.parent, pathBuilder);
        }
        pathBuilder.append(parent.getProperty());
        if (parent != this.parent) {
            pathBuilder.append('.');
        }
    }
}