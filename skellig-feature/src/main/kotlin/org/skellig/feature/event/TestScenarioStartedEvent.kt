package org.skellig.feature.event

import org.skellig.feature.SkelligTestEntity

class TestScenarioStartedEvent(
    val testScenario: SkelligTestEntity,
    val featureId: Int
) : SkelligTimedEvent()