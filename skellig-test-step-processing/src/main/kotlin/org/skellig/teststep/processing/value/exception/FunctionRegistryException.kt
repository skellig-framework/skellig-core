package org.skellig.teststep.processing.value.exception

class FunctionRegistryException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)
}