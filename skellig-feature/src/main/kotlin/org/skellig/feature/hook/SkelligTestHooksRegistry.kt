package org.skellig.feature.hook

interface SkelligTestHooksRegistry {

    fun getByTags(hookType: Class<*>, tags: Set<String>?): Collection<SkelligHook>

    fun getHooks(): Collection<SkelligHook>
}