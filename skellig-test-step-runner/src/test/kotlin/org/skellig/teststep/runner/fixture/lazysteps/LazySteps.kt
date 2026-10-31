package org.skellig.teststep.runner.fixture.lazysteps

import org.skellig.teststep.runner.annotation.TestStep
import java.util.concurrent.atomic.AtomicInteger

class LazySteps {

    init {
        created.incrementAndGet()
    }

    @TestStep(name = "lazy step")
    fun step() {
    }

    companion object {
        val created = AtomicInteger()
    }
}
