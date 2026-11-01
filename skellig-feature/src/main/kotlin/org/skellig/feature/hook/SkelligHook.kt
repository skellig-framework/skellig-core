package org.skellig.feature.hook

import org.skellig.teststep.processing.state.ScopeParameters
import org.skellig.teststep.processing.state.TestScenarioScope
import java.lang.reflect.Method

/**
 * SkelligHook represents a hook that can be executed before or after a test scenario or a test feature.
 *
 * @property tags The set of tags associated with the hook. If null or empty, all hooks will be matched.
 * @property method The method to be invoked when the hook is executed.
 * @property hookClass The class that contains the hook method. The method is invoked on the instance of this class
 * which belongs to the parallel group of the scope the hook runs in.
 * @property order The order in which the hook should be executed. Hooks with lower order values will be executed first.
 * @property type The type of the hook annotation. It can be one of the following: [BeforeTestScenario], [AfterTestScenario], [BeforeTestFeature], [AfterTestFeature].
 */
class SkelligHook(
    val tags: Set<String>?,
    val method: Method,
    val hookClass: Class<*>,
    val order: Int = 0,
    val type: Class<out Annotation>
) {
    /**
     * Executes the method associated with a [SkelligHook] instance.
     * This method uses reflection to invoke the method on the instance of the [hookClass] of the parallel group of the [scope].
     * A parameter of the method of type `TestScenarioState` receives the state of the [scope],
     * and a parameter of type `TestScenarioScope` receives the [scope] itself.
     *
     * @param scope The scope of the scenario, or of the feature, the hook runs for.
     * @throws Throwable if an exception occurs during the invocation of the method.
     *
     * @see SkelligHook
     */
    fun run(scope: TestScenarioScope) {
        val instance = scope.parallelGroup.instanceOf(hookClass)
        method.invoke(instance, *method.parameterTypes.map { ScopeParameters.valueFor(it, scope) }.toTypedArray())
    }
}
