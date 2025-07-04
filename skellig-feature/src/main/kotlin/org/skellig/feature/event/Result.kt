package org.skellig.feature.event

class Result(
    var duration: Long,
    var error: Throwable?,
    var errorLog: String?,
    var result: Any?,
    var executionStatus: TestExecutionStatus,
) {
    constructor() : this(0L, null, null, null, TestExecutionStatus.IGNORED)

    constructor(
        duration: Long,
        error: Throwable?,
        result: Any?
    ) : this(
        duration,
        error,
        error?.stackTraceToString(), result,
        if (error != null) TestExecutionStatus.FAILED else TestExecutionStatus.PASSED
    )

}