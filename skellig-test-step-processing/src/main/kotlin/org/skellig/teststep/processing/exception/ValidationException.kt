package org.skellig.teststep.processing.exception

/**
 * Exception thrown when a validation error occurs when checking [validation details][org.skellig.teststep.processing.model.DefaultTestStep.validationDetails].
 * of a test step.
 *
 * @param message The error message describing the validation failure.
 * @param cause The cause of the validation failure (optional).
 */
class ValidationException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)

    constructor(e: Exception?) : this("", e)

}