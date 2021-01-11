package org.skellig.task.exception

class TaskRunException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {

    constructor(message: String?) : this(message, null)

}