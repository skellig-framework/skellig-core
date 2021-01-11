package org.skellig.teststep.processing.exception

class TestStepProcessingException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}