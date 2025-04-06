package org.skellig.runner.plugin

import org.skellig.feature.event.SkelligTestEventDispatcher

interface SkelligPlugin {

    fun init(eventDispatcher: SkelligTestEventDispatcher)

    fun getName(): String
}