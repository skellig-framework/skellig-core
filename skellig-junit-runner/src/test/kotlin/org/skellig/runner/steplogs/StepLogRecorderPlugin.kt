package org.skellig.runner.steplogs

import org.skellig.feature.event.SkelligTestEventDispatcher
import org.skellig.feature.event.TestStepFinishedEvent
import org.skellig.feature.event.TestStepStartedEvent
import org.skellig.plugin.SkelligPlugin
import java.util.concurrent.ConcurrentHashMap

/**
 * Records the log records which are reported for each test step by its name.
 */
class StepLogRecorderPlugin(private val name: String) : SkelligPlugin {

    companion object {
        val stepLogs = ConcurrentHashMap<String, List<String>>()
    }

    private val stepNames = ConcurrentHashMap<Int, String>()

    override fun init(eventDispatcher: SkelligTestEventDispatcher) {
        eventDispatcher.register(TestStepStartedEvent::class) { e -> stepNames[e.testStep.getId()] = e.testStep.name }
        eventDispatcher.register(TestStepFinishedEvent::class) { e ->
            stepNames[e.testStepId]?.let { stepLogs[it] = e.logRecords ?: emptyList() }
        }
    }

    override fun getName(): String = name
}
