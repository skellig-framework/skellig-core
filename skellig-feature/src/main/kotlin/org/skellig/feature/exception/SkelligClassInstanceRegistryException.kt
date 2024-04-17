package org.skellig.feature.exception

/**
 * Exception thrown when there is an error registering a class instance of a hook.
 *
 * @param message The detail message.
 * @param cause The cause of the exception.
 */
class SkelligClassInstanceRegistryException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

}