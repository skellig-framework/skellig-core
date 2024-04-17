package org.skellig.teststep.processing.exception

/**
 * Represents an exception that occurs during the processing of a test step by
 * [TestStepProcessor][org.skellig.teststep.processing.processor.TestStepProcessor].
 *
 * @param message The detail message of the exception.
 * @param cause The cause of the exception.
 */
class TestStepProcessingException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}