package org.skellig.task.exception;

public class TaskRunException extends RuntimeException {

    public TaskRunException(String message) {
        super(message);
    }

    public TaskRunException(String message, Throwable cause) {
        super(message, cause);
    }
}
