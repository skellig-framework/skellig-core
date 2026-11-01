package org.skellig.feature.lazy

import org.skellig.feature.hook.annotation.BeforeTestScenario
import java.util.concurrent.atomic.AtomicInteger

class LazyHooks {

    init {
        created.incrementAndGet()
    }

    @BeforeTestScenario
    fun before() {
    }

    companion object {
        val created = AtomicInteger()
    }
}
