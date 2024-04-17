package org.skellig.feature.hook

import java.lang.reflect.Method

/**
 * SkelligHook represents a hook that can be executed before or after a test scenario or a test feature.
 *
 * @property tags The set of tags associated with the hook. If null or empty, all hooks will be matched.
 * @property method The method to be invoked when the hook is executed.
 * @property instance The instance of the class that contains the hook method.
 * @property order The order in which the hook should be executed. Hooks with lower order values will be executed first.
 * @property type The type of the hook annotation. It can be one of the following: [BeforeTestScenario], [AfterTestScenario], [BeforeTestFeature], [AfterTestFeature].
 */
class SkelligHook(
    val tags: Set<String>?,
    val method: Method,
    val instance: Any,
    val order: Int = 0,
    val type: Class<out Annotation>
) {
    /**
     * Executes the method associated with a [SkelligHook] instance.
     * This method uses reflection to invoke the method on the provided instance.
     *
     * @throws Throwable if an exception occurs during the invocation of the method.
     *
     * @see SkelligHook
     */
    fun run() {
        method.invoke(instance)
    }
}