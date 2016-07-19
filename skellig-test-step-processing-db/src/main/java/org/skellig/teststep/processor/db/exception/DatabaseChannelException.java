package org.skellig.teststep.processor.db.exception;

public class DatabaseChannelException extends RuntimeException {

    public DatabaseChannelException(String message) {
        super(message);
    }

    public DatabaseChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
