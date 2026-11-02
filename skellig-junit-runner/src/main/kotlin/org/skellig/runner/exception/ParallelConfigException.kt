package org.skellig.runner.exception

/**
 * Exception to be thrown if a setting of `skellig.parallel` in the configuration cannot be used: it has an invalid
 * value, or a value which is valid but not supported yet.
 *
 * @param message The detail message, which says which setting is invalid and what it can be.
 * @param cause The cause of the exception.
 */
class ParallelConfigException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
