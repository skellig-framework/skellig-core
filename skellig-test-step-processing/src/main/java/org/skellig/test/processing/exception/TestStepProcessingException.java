package org.skellig.test.processing.exception;

public class TestStepProcessingException extends RuntimeException {

    public TestStepProcessingException(String message) {
        super(message);
    }

    public TestStepProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
