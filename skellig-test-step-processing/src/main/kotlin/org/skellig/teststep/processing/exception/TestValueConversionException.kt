package org.skellig.teststep.processing.exception

class TestValueConversionException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}