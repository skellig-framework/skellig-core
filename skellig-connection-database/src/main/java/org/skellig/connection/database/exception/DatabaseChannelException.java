package org.skellig.connection.database.exception;

public class DatabaseChannelException extends RuntimeException {

    public DatabaseChannelException(String message) {
        super(message);
    }

    public DatabaseChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
