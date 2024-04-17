package org.skellig.teststep.processing.exception

/**
 * Exception thrown when there is an error creating a test step.
 *
 * @param message The detail message of the exception.
 * @param cause The cause of the exception.
 */
class TestStepCreationException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}