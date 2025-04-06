package org.skellig.feature.event

abstract class BaseTestStepExecutionEvent(
    val testStepId: Int,
    val featureId: Int,
    val testScenarioId: Int?,
    val executionSequenceType: ExecutionSequenceType,
) : SkelligTimedEvent()