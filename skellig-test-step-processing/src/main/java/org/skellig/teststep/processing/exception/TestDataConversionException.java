package org.skellig.teststep.processing.exception;

public class TestDataConversionException extends RuntimeException {

    public TestDataConversionException(String message) {
        super(message);
    }

    public TestDataConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
