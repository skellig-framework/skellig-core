package org.skellig.feature.event

class TestScenarioFinishedEvent(val testScenarioId: Any, val featureId: Int) : SkelligTimedEvent()