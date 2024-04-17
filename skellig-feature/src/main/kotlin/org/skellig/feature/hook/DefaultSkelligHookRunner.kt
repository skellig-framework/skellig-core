package org.skellig.feature.hook

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import kotlin.system.measureTimeMillis

/**
 * DefaultSkelligHookRunner is a class that implements the SkelligHookRunner interface.
 * It is responsible for running hooks based on the specified tags and hook type.
 *
 * @param testHooksRegistry The SkelligTestHooksRegistry instance used to retrieve the hooks based on tags and hook type.
 */
class DefaultSkelligHookRunner(private val testHooksRegistry: SkelligTestHooksRegistry) : SkelligHookRunner {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DefaultSkelligHookRunner::class.java)
    }

    override fun run(tags: Set<String>?, hookType: Class<out Annotation>, onRunCompleted: (String, Throwable?, Long) -> Unit) {
        testHooksRegistry.getByTags(hookType, tags)
            .sortedBy { it.order }
            .forEach {
                val fullName = "${it.instance::class.java.name}.${it.method.name}"
                LOGGER.info("Run $fullName hook")
                try {
                    val duration = measureTimeMillis { it.run() }
                    onRunCompleted.invoke(fullName, null, duration)
                } catch (e: InvocationTargetException) {
                    LOGGER.error("Failed to run the $fullName hook")
                    onRunCompleted.invoke(fullName, e.targetException, 0)
                }
            }
    }
}