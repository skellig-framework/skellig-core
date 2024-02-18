package org.skellig.feature

import org.skellig.feature.metadata.TestMetadata

open class Feature protected constructor(val name: String,
                                         val scenarios: List<TestScenario>?,
                                         val testPreRequisites: List<TestMetadata<*>>?) {

    class Builder {

        private var name: String? = null
        private val scenarios = mutableListOf<TestScenario>()
        private var testPreRequisites: List<TestMetadata<*>>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withScenarios(scenarios: List<TestScenario>) = apply { this.scenarios.addAll(scenarios) }

        fun withTestPreRequisites(testPreRequisites: List<TestMetadata<*>>?) = apply { this.testPreRequisites = testPreRequisites }

        fun build(): Feature {
            return Feature(name?: error("Feature cannot have empty name"),
                           scenarios, testPreRequisites)
        }
    }
}