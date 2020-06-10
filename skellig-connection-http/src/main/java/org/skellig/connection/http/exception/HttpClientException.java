package org.skellig.connection.http.exception;

public class HttpClientException extends RuntimeException {

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpClientException(String message) {
        super(message);
    }
}
