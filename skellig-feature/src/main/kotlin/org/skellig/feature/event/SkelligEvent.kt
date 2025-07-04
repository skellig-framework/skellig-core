package org.skellig.feature.event

import java.time.Instant

interface SkelligEvent {
    fun getEventTime(): Instant
}