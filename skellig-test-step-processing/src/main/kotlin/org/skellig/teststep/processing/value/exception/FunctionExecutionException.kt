package org.skellig.teststep.processing.value.exception

import org.skellig.teststep.processing.value.function.Function

/**
 * Represents an exception that occurs during registration of functions marked with @[Function].
 *
 * @param message A detailed message explaining the cause of the exception
 * @param cause The underlying cause of the exception, or null if none
 */
class FunctionExecutionException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}