package org.skellig.teststep.processing.value.exception

/**
 * Exception class for errors that occur in the FunctionRegistry.
 *
 * @param message A detailed message explaining the cause of the exception
 * @param cause The underlying cause of the exception, or null if none
 */
class FunctionRegistryException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}