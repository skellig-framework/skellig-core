package org.skellig.teststep.processing.fixture.lazyfunctions

import org.skellig.teststep.processing.value.function.Function
import java.util.concurrent.atomic.AtomicInteger

class LazyFunctions {

    init {
        created.incrementAndGet()
    }

    @Function
    fun something(): String = "something"

    companion object {
        val created = AtomicInteger()
    }
}
