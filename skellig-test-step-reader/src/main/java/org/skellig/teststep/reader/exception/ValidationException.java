package org.skellig.teststep.reader.exception;

public class ValidationException extends RuntimeException {

    private String testStepId;

    public ValidationException(Exception e) {
        super(e);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String testStepId) {
        this(message);
        this.testStepId = testStepId;
    }

    public ValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public String getTestStepId() {
        return testStepId;
    }
}
