package org.skellig.teststep.processing.exception

class TestDataProcessingInitException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}