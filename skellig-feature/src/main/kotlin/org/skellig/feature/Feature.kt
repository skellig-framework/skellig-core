package org.skellig.feature


open class Feature protected constructor(
    val name: String,
    val scenarios: List<TestScenario>?,
    val beforeFeatureSteps: List<TestStep>?,
    val beforeTestScenarioSteps: List<TestStep>?,
    val afterFeatureSteps: List<TestStep>?,
    val afterTestScenarioSteps: List<TestStep>?,
    val tags: Set<String>?
) : SkelligTestEntity {

    override fun getEntityName(): String = name

    override fun getEntityTags(): Set<String>? = tags

    class Builder {

        private var name: String? = null
        private val scenarios = mutableListOf<TestScenario>()
        private var beforeFeatureStepsBuilder: MutableList<TestStep.Builder>? = null
        private var beforeTestScenarioStepsBuilder: MutableList<TestStep.Builder>? = null
        private var afterFeatureStepsBuilder: MutableList<TestStep.Builder>? = null
        private var afterTestScenarioStepsBuilder: MutableList<TestStep.Builder>? = null
        private var tags: Set<String>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withScenarios(scenarioBuilder: TestScenario.Builder) = apply {
            tags?.let { scenarioBuilder.withTags(it) }
            this.scenarios.addAll(scenarioBuilder.build())
        }

        fun withBeforeFeatureStep(stepBuilder: TestStep.Builder) = apply {
            if (beforeFeatureStepsBuilder == null) beforeFeatureStepsBuilder = mutableListOf()
            beforeFeatureStepsBuilder?.add(stepBuilder)
        }

        fun withBeforeTestScenarioStep(stepBuilder: TestStep.Builder) = apply {
            if (beforeTestScenarioStepsBuilder == null) beforeTestScenarioStepsBuilder = mutableListOf()
            beforeTestScenarioStepsBuilder?.add(stepBuilder)
        }

        fun withAfterFeatureStep(stepBuilder: TestStep.Builder) = apply {
            if (afterFeatureStepsBuilder == null) afterFeatureStepsBuilder = mutableListOf()
            afterFeatureStepsBuilder?.add(stepBuilder)
        }

        fun withAfterTestScenarioStep(stepBuilder: TestStep.Builder) = apply {
            if (afterTestScenarioStepsBuilder == null) afterTestScenarioStepsBuilder = mutableListOf()
            afterTestScenarioStepsBuilder?.add(stepBuilder)
        }

        fun withTags(tags: Set<String>?) = apply { this.tags = tags }

        fun build(): Feature {
            return Feature(
                name ?: error("Feature cannot have empty name"),
                scenarios,
                beforeFeatureStepsBuilder?.map { it.build() }?.toList(),
                beforeTestScenarioStepsBuilder?.map { it.build() }?.toList(),
                afterFeatureStepsBuilder?.map { it.build() }?.toList(),
                afterTestScenarioStepsBuilder?.map { it.build() }?.toList(),
                tags
            )
        }
    }
}