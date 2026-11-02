package org.skellig.teststep.processing.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.log4j.MDC

/**
 * Runs the [block] while the workers of the shared coroutine dispatchers have [scopeId] in their log context, like when
 * a scenario created them or ran on them before, and removes it from them after. A test finds out if the code under
 * test relies on the log context of a worker instead of giving the scope to it.
 */
fun <T> withCoroutineWorkersOfScope(scopeId: String, block: () -> T): T {
    setScopeIdOfCoroutineWorkers(scopeId)
    try {
        return block()
    } finally {
        setScopeIdOfCoroutineWorkers(null)
    }
}

/**
 * Many blocked coroutines make the dispatcher use all its workers, so each of them gets the scope id.
 */
private fun setScopeIdOfCoroutineWorkers(scopeId: String?) {
    listOf(Dispatchers.Default, Dispatchers.IO).forEach { dispatcher ->
        runBlocking {
            withContext(dispatcher) {
                coroutineScope {
                    repeat(128) {
                        launch {
                            if (scopeId == null) MDC.remove(ScopeLogContext.KEY) else MDC.put(ScopeLogContext.KEY, scopeId)
                            Thread.sleep(20)
                        }
                    }
                }
            }
        }
    }
}
