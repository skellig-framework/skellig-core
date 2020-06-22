package org.skellig.teststep.processing.exception;

public class TestValueConversionException extends RuntimeException {

    public TestValueConversionException(String message) {
        super(message);
    }

    public TestValueConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
