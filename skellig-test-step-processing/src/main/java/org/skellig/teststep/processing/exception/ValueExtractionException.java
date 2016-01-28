package org.skellig.teststep.processing.exception;

public class ValueExtractionException extends RuntimeException {

    public ValueExtractionException(String message) {
        super(message);
    }

    public ValueExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
