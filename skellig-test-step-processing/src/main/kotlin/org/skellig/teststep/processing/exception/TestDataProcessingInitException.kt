package org.skellig.teststep.processing.exception

/**
 * Exception class used for signaling an initialization error when Test Step processor is created.
 *
 * @param message The detail message of the exception.
 * @param cause The cause of the exception.
 */
class TestDataProcessingInitException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}