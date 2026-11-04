package org.skellig.runner.parallelfailure

import org.skellig.feature.event.FeatureStartedEvent
import org.skellig.feature.event.SkelligTestEventDispatcher
import org.skellig.plugin.SkelligPlugin

/**
 * Fails when a feature with a name which starts with [featureNamePrefix] starts, with something which the runner of
 * the feature does not expect, as a plugin which fails would.
 */
class FailingFeaturePlugin(private val featureNamePrefix: String) : SkelligPlugin {

    override fun init(eventDispatcher: SkelligTestEventDispatcher) {
        eventDispatcher.register(FeatureStartedEvent::class) { e ->
            if (e.feature.name.startsWith(featureNamePrefix)) throw IllegalStateException("Report failed for ${e.feature.name}")
        }
    }

    override fun getName(): String = "failing-feature"
}
