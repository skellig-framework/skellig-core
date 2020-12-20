package org.skellig.teststep.processing.exception

class ValueExtractionException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}