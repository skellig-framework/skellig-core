package org.skellig.teststep.processing.state

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Default implementation of the [TestScenarioState] interface.
 * It uses [HashMap] to store the state data and all methods are thread-safe
 * using [ReentrantReadWriteLock] for all available write and read operations.
 */
class DefaultTestScenarioState : TestScenarioState {

    private val state: MutableMap<String, Any?> = mutableMapOf()
    private val lock = ReentrantReadWriteLock()

    override fun get(key: String?): Any? {
        return key?.let {
            return lock.read { state[key] }
        }
    }

    override fun set(key: String?, value: Any?) {
        key?.let {
            lock.write { state[key] = value }
        }
    }

    override fun remove(key: String?) {
        key?.let { lock.write { state.remove(key) } }
    }

    override fun clean() {
        lock.write { state.clear() }
    }

    override fun iterator(): Iterator<Pair<String, Any?>> = lock.read { StateIterator() }

    private inner class StateIterator : Iterator<Pair<String, Any?>> {

        var iterator = state.entries.map { Pair(it.key, it.value) }.iterator()

        override fun hasNext() = iterator.hasNext()

        override fun next(): Pair<String, Any?> = iterator.next()
    }
}