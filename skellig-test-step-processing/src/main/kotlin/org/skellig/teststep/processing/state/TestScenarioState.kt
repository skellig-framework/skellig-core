package org.skellig.teststep.processing.state

interface TestScenarioState : Iterable<Pair<String, Any?>> {

    fun get(key: String?): Any?

    fun set(key: String?, value: Any?)

    fun remove(key: String?)

    fun clean()
}