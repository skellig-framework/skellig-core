package org.skellig.teststep.reader.exception

/**
 * Exception thrown when there is an error reading a test step.
 *
 * @param message The error message.
 * @param cause The cause of the exception.
 */
class TestStepReadException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)

    constructor(cause: Throwable?) : this(null, cause)

}