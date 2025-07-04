package org.skellig.feature.event

class TestStepProcessingFinishedEvent(
    testStepId: Int,
    featureId: Int,
    testScenarioId: Int?,
    var result: Result,
    var testStepInfo: Map<String, String>,
    executionSequenceType: ExecutionSequenceType = ExecutionSequenceType.NORMAL,
) : BaseTestStepExecutionEvent(testStepId, featureId, testScenarioId, executionSequenceType) {

}