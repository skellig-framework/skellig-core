package org.skellig.teststep.processing.util

import kotlinx.coroutines.ThreadContextElement
import org.apache.log4j.MDC
import org.skellig.teststep.processing.state.TestScenarioScope
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Marks every log event of the thread with the id of the [TestScenarioScope] the thread works for, in the log context
 * (MDC) of log4j under [KEY], so a log appender can attribute the event to the scope whatever its message is.
 * The MDC of log4j is used directly, as the one of slf4j can be bound to a temporary adapter which never reaches
 * log4j, when it is first used while slf4j is still initializing.
 *
 * The log context belongs to a thread, so the code that hands work over to another thread (a pool or coroutines)
 * must carry the scope there on its own, with [runWith] or [coroutineContext]. It is only for routing log events:
 * the scope itself always travels with the parameters of the test step. A thread which was not given the scope
 * logs events without it, and the log appender does not attribute them to any scope.
 *
 * A thread inherits the log context of the thread that creates it, which is not what a worker of a shared pool needs,
 * as it can then serve any scope. Therefore a scope is always set for the time of the work, and the previous one is
 * restored after it.
 */
object ScopeLogContext {

    /**
     * The key in the log context which holds the id of the scope.
     */
    const val KEY = "skellig.scope"

    /**
     * Runs the [block] on the current thread with the id of the [scope] in its log context, and restores the previous
     * one after. Without the [scope], the block runs without any scope id in the log context.
     */
    fun <T> runWith(scope: TestScenarioScope?, block: () -> T): T {
        val previousScopeId = currentScopeId()
        setScopeId(scope?.id)
        try {
            return block()
        } finally {
            setScopeId(previousScopeId)
        }
    }

    /**
     * Gives the coroutines which run in it, on any thread, the id of the [scope] in the log context of the thread
     * they run on. Child coroutines inherit it, so it is enough to add it to the context of the coroutine that starts
     * the others. A coroutine of the scope can be resumed on another thread, so it can not rely on the log context
     * of the thread that started it. Without the [scope], the coroutines run without any scope id.
     */
    fun coroutineContext(scope: TestScenarioScope?): CoroutineContext.Element = ScopeIdElement(scope?.id)

    /**
     * The id of the scope in the log context of the current thread, or null if the thread was not given a scope.
     */
    fun currentScopeId(): String? = MDC.get(KEY)?.toString()

    private fun setScopeId(scopeId: String?) {
        if (scopeId == null) MDC.remove(KEY) else MDC.put(KEY, scopeId)
    }

    private class ScopeIdElement(private val scopeId: String?) : AbstractCoroutineContextElement(Key), ThreadContextElement<String?> {

        companion object Key : CoroutineContext.Key<ScopeIdElement>

        override fun updateThreadContext(context: CoroutineContext): String? {
            val previousScopeId = currentScopeId()
            setScopeId(scopeId)
            return previousScopeId
        }

        override fun restoreThreadContext(context: CoroutineContext, oldState: String?) {
            setScopeId(oldState)
        }
    }
}
