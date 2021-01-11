package org.skellig.teststep.reader.exception

class TestStepReadException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)

    constructor(cause: Throwable?) : this(null, cause)

}