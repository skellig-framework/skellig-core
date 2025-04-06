package org.skellig.feature.event

import org.skellig.feature.TestStep

class TestStepStartedEvent(
    val testStep: TestStep,
    val parentFeatureId: Int,
    val parentTestScenarioId: Int?,
    val parameters: Map<String, Any?>?,
    val executionSequenceType: ExecutionSequenceType = ExecutionSequenceType.NORMAL,
) : SkelligTimedEvent()