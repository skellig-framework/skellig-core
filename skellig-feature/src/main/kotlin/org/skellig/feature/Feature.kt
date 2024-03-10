package org.skellig.feature


open class Feature protected constructor(
    val name: String,
    val scenarios: List<TestScenario>?,
    val beforeSteps: List<TestStep>?,
    val afterSteps: List<TestStep>?,
    val tags: Set<String>?
) : SkelligTestEntity {

    override fun getEntityName(): String = name

    override fun getEntityTags(): Set<String>? = tags

    class Builder {

        private var name: String? = null
        private val testScenarioBuilders = mutableListOf<TestScenario.Builder>()
        private var beforeFeatureStepsBuilder: MutableList<TestStep.Builder>? = null
        private var beforeTestScenarioStepsBuilder: MutableList<TestStep.Builder>? = null
        private var afterFeatureStepsBuilder: MutableList<TestStep.Builder>? = null
        private var afterTestScenarioStepsBuilder: MutableList<TestStep.Builder>? = null
        private var tags: Set<String>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withTestScenario(scenarioBuilder: TestScenario.Builder) = apply {
            tags?.let { scenarioBuilder.withTags(it) }
            this.testScenarioBuilders.add(scenarioBuilder)
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
            val beforeTestScenarioSteps = beforeTestScenarioStepsBuilder?.map { it.build() }?.toList()
            val afterTestScenarioSteps = afterTestScenarioStepsBuilder?.map { it.build() }?.toList()
            val testScenarios = testScenarioBuilders.flatMap {
                it.withBeforeSteps(beforeTestScenarioSteps)
                    .withAfterSteps(afterTestScenarioSteps)
                    .build()
            }.toList()

            return Feature(
                name ?: error("Feature cannot have an empty name"),
                testScenarios,
                beforeFeatureStepsBuilder?.map { it.build() }?.toList(),
                afterFeatureStepsBuilder?.map { it.build() }?.toList(),
                tags
            )
        }
    }
}