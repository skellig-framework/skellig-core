package org.skellig.feature


/**
 * Represents a feature in a test suite, which is constructed from a Skellig feature file
 *
 * @property filePath The file path of the feature.
 * @property name The name of the feature.
 * @property scenarios The list of test scenarios associated with the feature. It can be null if there are no scenarios.
 * @property beforeSteps The list of test steps to be executed before the feature. It can be null if there are no before steps.
 * @property afterSteps The list of test steps to be executed after the feature. It can be null if there are no after steps.
 * @property tags The set of tags associated with the feature. It can be null if there are no tags.
 */
open class Feature protected constructor(
    val filePath: String,
    val name: String,
    val scenarios: List<TestScenario>?,
    val beforeSteps: List<TestStep>?,
    val afterSteps: List<TestStep>?,
    val tags: Set<String>?
) : SkelligTestEntity {

    override fun getEntityName(): String = name

    override fun getEntityTags(): Set<String>? = tags

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Feature

        return filePath == other.filePath
    }

    override fun hashCode(): Int = filePath.hashCode()

    class Builder {

        private var name: String? = null
        private var filePath: String? = null
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

        fun withFilePath(filePath: String) = apply { this.filePath = filePath }

        fun build(): Feature {
            val filePath = filePath ?: error("Feature cannot have an empty file path")

            val testScenarios =
                testScenarioBuilders.flatMapIndexed { i, builder ->
                    builder
                        .withPosition(i)
                        .withParent(filePath)
                        .withBeforeSteps(beforeTestScenarioStepsBuilder)
                        .withAfterSteps(afterTestScenarioStepsBuilder)
                        .build()
                }.toList()

            var beforeAfterStepsCounter = 0
            return Feature(
                filePath,
                name ?: error("Feature cannot have an empty name"),
                testScenarios,
                beforeFeatureStepsBuilder?.map { builder ->
                    builder.withParent(filePath).withPosition(beforeAfterStepsCounter++).build()
                }?.toList(),
                afterFeatureStepsBuilder?.map { builder ->
                    builder.withParent(filePath).withPosition(beforeAfterStepsCounter++).build()
                }?.toList(),
                tags
            )
        }
    }
}