package org.skellig.teststep.processing.value.extractor.exception

class ValueExtractionException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}