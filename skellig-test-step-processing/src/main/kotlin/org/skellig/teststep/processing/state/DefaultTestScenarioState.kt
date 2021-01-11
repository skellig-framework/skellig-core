package org.skellig.teststep.processing.state

import java.util.concurrent.ConcurrentHashMap

class DefaultTestScenarioState : TestScenarioState {

    private var state: MutableMap<String, Any?> = ConcurrentHashMap()

    override fun get(key: String?): Any? {
        return if (key == null) null else state[key]
    }

    override fun set(key: String?, value: Any?) {
        key.let { state[key!!] = value }
    }

    override fun remove(key: String?) {
        key.let { state.remove(key) }
    }

    override fun clean() {
        state.clear()
    }
}