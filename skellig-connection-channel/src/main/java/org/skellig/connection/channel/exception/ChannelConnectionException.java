package org.skellig.connection.channel.exception;

public class ChannelConnectionException extends RuntimeException {

    public ChannelConnectionException(Throwable cause) {
        super(cause);
    }

    public ChannelConnectionException(String message) {
        super(message);
    }
}
