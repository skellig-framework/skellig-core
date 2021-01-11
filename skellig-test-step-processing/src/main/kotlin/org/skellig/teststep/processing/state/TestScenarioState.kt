package org.skellig.teststep.processing.state

interface TestScenarioState {

    fun get(key: String?): Any?

    fun set(key: String?, value: Any?)

    fun remove(key: String?)

    fun clean()
}