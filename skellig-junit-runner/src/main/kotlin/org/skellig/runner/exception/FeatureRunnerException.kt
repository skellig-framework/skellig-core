package org.skellig.runner.exception

/**
 * Exception to be thrown if failed to read a Skellig feature file.
 *
 * @param message The detail message.
 * @param cause The cause of the exception.
 */
class FeatureRunnerException(message: String?, cause: Throwable?) : RuntimeException(message, cause)