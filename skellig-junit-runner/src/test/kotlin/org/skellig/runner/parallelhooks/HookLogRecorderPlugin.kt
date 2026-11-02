package org.skellig.runner.parallelhooks

import org.skellig.feature.event.HookFinishedEvent
import org.skellig.feature.event.SkelligTestEventDispatcher
import org.skellig.plugin.SkelligPlugin
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Records the log records which are reported for each hook, with the name of the thread which reported them.
 */
class HookLogRecorderPlugin(private val name: String) : SkelligPlugin {

    /**
     * @property thread The thread which reported the hook.
     * @property logRecords The log records reported for the hook.
     */
    class HookLog(val thread: String, val logRecords: List<String>)

    companion object {
        val hookLogs = CopyOnWriteArrayList<HookLog>()
    }

    override fun init(eventDispatcher: SkelligTestEventDispatcher) {
        eventDispatcher.register(HookFinishedEvent::class) { e ->
            hookLogs.add(HookLog(Thread.currentThread().name, e.logRecords ?: emptyList()))
        }
    }

    override fun getName(): String = name
}
