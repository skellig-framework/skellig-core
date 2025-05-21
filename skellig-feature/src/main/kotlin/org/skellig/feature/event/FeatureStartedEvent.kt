package org.skellig.feature.event

import org.skellig.feature.Feature

class FeatureStartedEvent(
    val feature: Feature,
) : SkelligTimedEvent()