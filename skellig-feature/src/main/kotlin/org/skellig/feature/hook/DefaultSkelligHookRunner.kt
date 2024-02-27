package org.skellig.feature.hook

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

class DefaultSkelligHookRunner(private val testHooksRegistry: SkelligTestHooksRegistry) : SkelligHookRunner {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DefaultSkelligHookRunner::class.java)
    }

    override fun run(tags: Set<String>?, hookType: Class<out Annotation>, onRunCompleted: (String, Throwable?, Long) -> Unit) {
        testHooksRegistry.getByTags(hookType, tags)
            .sortedBy { it.order }
            .forEach {
                val fullName = "${it.instance::class.java}.${it.method.name}"
                var duration = 0L
                try {
                     duration = measureTimeMillis { it.run() }
                } catch (e: Throwable) {
                    onRunCompleted.invoke(fullName, e, duration)
                } finally {
                    onRunCompleted.invoke(fullName, null, duration)
                }
            }
    }
}