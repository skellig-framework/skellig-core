package org.skellig.teststep.reader.exception;

public class TestStepReadException extends RuntimeException {

    public TestStepReadException(Exception e) {
        super(e);
    }

    public TestStepReadException(String message) {
        super(message);
    }
}
