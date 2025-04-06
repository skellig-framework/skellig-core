package org.skellig.feature.event

import java.time.Instant

abstract class SkelligTimedEvent : SkelligEvent {

    private val eventTime: Instant = Instant.now()

    override fun getEventTime(): Instant = eventTime
}