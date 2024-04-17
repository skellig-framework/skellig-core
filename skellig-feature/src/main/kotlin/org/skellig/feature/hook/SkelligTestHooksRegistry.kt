package org.skellig.feature.hook

/**
 * SkelligTestHooksRegistry is an interface that defines the contract for managing and retrieving [SkelligHook]s.
 */
interface SkelligTestHooksRegistry {

    /**
     * Retrieves a collection of [SkelligHook] objects based on the specified hook type and tags.
     *
     * @param hookType The type of hook. Examples of hook types include:
     *                 - [BeforeTestFeature]
     *                 - [BeforeTestScenario]
     *                 - [AfterTestFeature]
     *                 - [BeforeTestScenario]
     * @param tags The set of tags to filter out hooks. If null or empty, all hooks will be retrieved.
     * @return A collection of [SkelligHook] objects that match the specified hook type and tags.
     */
    fun getByTags(hookType: Class<*>, tags: Set<String>?): Collection<SkelligHook>

    /**
     * Retrieves a collection of all [SkelligHook].
     *
     * @return A collection of [SkelligHook] objects.
     */
    fun getHooks(): Collection<SkelligHook>
}