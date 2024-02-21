package org.skellig.feature


open class Feature protected constructor(
    val name: String,
    val scenarios: List<TestScenario>?,
    val tags: Set<String>?
) {

    class Builder {

        private var name: String? = null
        private val scenarios = mutableListOf<TestScenario>()
        private var tags: Set<String>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withScenarios(scenarioBuilder: TestScenario.Builder) = apply {
            tags?.let { scenarioBuilder.withTags(it) }
            this.scenarios.addAll(scenarioBuilder.build())
        }

        fun withTags(tags: Set<String>?) = apply { this.tags = tags }

        fun build(): Feature {
            return Feature(
                name ?: error("Feature cannot have empty name"),
                scenarios, tags
            )
        }
    }
}