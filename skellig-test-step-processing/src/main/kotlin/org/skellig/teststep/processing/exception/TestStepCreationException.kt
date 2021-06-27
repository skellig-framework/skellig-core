package org.skellig.teststep.processing.exception

class TestStepCreationException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}