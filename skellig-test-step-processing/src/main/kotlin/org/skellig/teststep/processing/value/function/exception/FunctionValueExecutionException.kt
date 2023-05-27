package org.skellig.teststep.processing.value.function.exception

class FunctionValueExecutionException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}