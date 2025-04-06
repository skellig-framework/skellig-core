package org.skellig.feature.event

class TestStepFinishedEvent(
    testStepId: Int,
    featureId: Int,
    testScenarioId: Int?,
    var executionStatus: TestExecutionStatus,
    var logRecords: List<String>?,
    var parameters: Map<String, Any?>?,
    executionSequenceType: ExecutionSequenceType,
) : BaseTestStepExecutionEvent(testStepId, featureId, testScenarioId, executionSequenceType) {

    constructor(
        testStepId: Int,
        featureId: Int,
        testScenarioId: Int?,
        executionSequenceType: ExecutionSequenceType
    ) : this(testStepId, featureId, testScenarioId, TestExecutionStatus.RUNNING, null, null, executionSequenceType)
}