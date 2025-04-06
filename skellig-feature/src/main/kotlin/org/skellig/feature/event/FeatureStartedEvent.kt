package org.skellig.feature.event

import org.skellig.feature.SkelligTestEntity

class FeatureStartedEvent(
    val feature: SkelligTestEntity,
) : SkelligTimedEvent()