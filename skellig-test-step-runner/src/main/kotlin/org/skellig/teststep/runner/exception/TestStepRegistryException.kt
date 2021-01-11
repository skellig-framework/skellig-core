package org.skellig.teststep.runner.exception

class TestStepRegistryException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    constructor(message: String?) : this(message, null)
}