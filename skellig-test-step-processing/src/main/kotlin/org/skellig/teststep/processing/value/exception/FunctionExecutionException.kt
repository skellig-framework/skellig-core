package org.skellig.teststep.processing.value.exception

class FunctionExecutionException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}