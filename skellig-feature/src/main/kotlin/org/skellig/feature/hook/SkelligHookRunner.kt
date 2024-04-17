package org.skellig.feature.hook

/**
 * SkelligHookRunner is an interface that defines the contract for running hooks based on tags and hook type.
 */
interface SkelligHookRunner {

    /**
     * Executes the given hooks based on the specified tags and hook type.
     *
     * @param tags The set of tags. If null or empty, all hooks will be executed.
     * @param hookType The type of hook annotation, such as: @[BeforeTestFeature][org.skellig.feature.hook.annotation.BeforeTestFeature],
     * [BeforeTestScenario][org.skellig.feature.hook.annotation.BeforeTestScenario], [AfterTestFeature][org.skellig.feature.hook.annotation.AfterTestFeature]
     * [BeforeTestScenario][org.skellig.feature.hook.annotation.BeforeTestScenario]
     * @param onRunCompleted The callback function to be called after each hook is executed. It takes three parameters:
     *                       hookName: The name of the executed hook.
     *                       e: The Throwable if an exception occurred during the execution of the hook, or null otherwise.
     *                       duration: The duration of the hook execution in milliseconds.
     */
    fun run(tags: Set<String>?, hookType: Class<out Annotation>, onRunCompleted: (String, Throwable?, Long) -> Unit)

}
