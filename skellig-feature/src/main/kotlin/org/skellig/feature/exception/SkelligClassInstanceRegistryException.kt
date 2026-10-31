package org.skellig.feature.exception

/**
 * Exception thrown when a hook cannot be registered: its class cannot be instantiated by a parallel group,
 * or its method declares a parameter which cannot be injected.
 *
 * @param message The detail message.
 * @param cause The cause of the exception.
 */
class SkelligClassInstanceRegistryException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

}