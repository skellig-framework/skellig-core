package org.skellig.teststep.runner.exception;

public class TestStepRegistryException extends RuntimeException {

    public TestStepRegistryException(String message) {
        super(message);
    }

    public TestStepRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
}
