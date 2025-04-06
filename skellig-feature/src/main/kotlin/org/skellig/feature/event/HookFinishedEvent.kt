package org.skellig.feature.event

class HookFinishedEvent(
    val methodName: String?,
    val parentFeatureId: Int,
    val parentTestScenarioId: Int?,
    val logRecords: List<String>?,
    val result: Result,
    val executionSequenceType: ExecutionSequenceType = ExecutionSequenceType.NORMAL,
) : SkelligTimedEvent()