package org.skellig.test.processing.exception;

public class TestStepRegistryException extends RuntimeException {

    public TestStepRegistryException(String message) {
        super(message);
    }

    public TestStepRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
