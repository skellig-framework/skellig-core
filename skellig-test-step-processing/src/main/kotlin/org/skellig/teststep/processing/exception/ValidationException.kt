package org.skellig.teststep.processing.exception

class ValidationException(message: String?, cause: Throwable?, val testStepId: String?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)

    constructor(e: Exception?) : this("", e, null)

    constructor(message: String?, testStepId: String?) : this(message, null, testStepId)

}