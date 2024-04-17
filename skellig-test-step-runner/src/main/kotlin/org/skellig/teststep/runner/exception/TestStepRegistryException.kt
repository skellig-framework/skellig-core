package org.skellig.teststep.runner.exception

/**
 * Exception thrown by the TestStepRegistry class when registration of a test step fails.
 *
 * @param message The detail message.
 * @param cause The cause of the exception.
 */
class TestStepRegistryException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}