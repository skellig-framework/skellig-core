package org.skellig.teststep.runner.exception;

public class TestStepMethodInvocationException extends RuntimeException {

    public TestStepMethodInvocationException(String message) {
        super(message);
    }

    public TestStepMethodInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
