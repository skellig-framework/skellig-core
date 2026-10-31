package org.skellig.feature.instances

import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestScenario

/**
 * Remembers the instances the hooks were called on, and the number of calls each instance got in its field.
 */
class InstanceHooks {

    private var calls = 0

    @BeforeTestScenario
    fun before() {
        calls++
        seen.add(this to calls)
    }

    @AfterTestScenario
    fun after() {
        calls++
        seen.add(this to calls)
    }

    companion object {
        val seen = mutableListOf<Pair<InstanceHooks, Int>>()
    }
}
