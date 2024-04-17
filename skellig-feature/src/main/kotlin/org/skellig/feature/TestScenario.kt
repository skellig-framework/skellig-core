package org.skellig.feature


/**
 * The TestScenario class represents a test scenario, which is constructed from a Skellig feature file.
 *
 * @param path The path of the test scenario.
 * @param name The name of the test scenario.
 * @param steps The list of test steps in the test scenario.
 * @param tags The set of tags associated with the test scenario.
 * @param beforeSteps The list of test steps to be executed before the test scenario.
 * @param afterSteps The list of test steps to be executed after the test scenario.
 */
open class TestScenario protected constructor(
    val path: String,
    val name: String,
    val steps: List<TestStep>?,
    val tags: Set<String>?,
    val beforeSteps: List<TestStep>?,
    val afterSteps: List<TestStep>?
) : SkelligTestEntity {

    override fun getEntityName(): String = name

    override fun getEntityTags(): Set<String>? = tags

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestScenario

        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    class Builder {
        private var name: String? = null
        private var parent: String? = null
        private var position = 0
        private var tags: Set<String>? = null
        private val stepBuilders = mutableListOf<TestStep.Builder>()
        private var data: MutableList<Pair<Set<String>?, MutableList<Map<String, String>>?>>? = null
        private var beforeSteps: List<TestStep.Builder>? = null
        private var afterSteps: List<TestStep.Builder>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withStep(stepBuilder: TestStep.Builder) = apply { stepBuilders.add(stepBuilder) }

        fun withTags(tags: Set<String>?) = apply {
            if (this.tags == null) this.tags = tags
            else if (tags != null) this.tags = this.tags!!.union(tags)
        }

        fun withData(tags: Set<String>?) = apply {
            if (data == null) {
                data = mutableListOf()
            }
            data?.add(Pair(tags, mutableListOf()))
        }

        fun withDataRows(dataRow: List<Map<String, String>>) = apply {
            data?.last()?.second?.addAll(dataRow) ?: error("Failed to add a data row for the scenario '$name' because the data table is not initialised")
        }

        fun withBeforeSteps(beforeSteps: List<TestStep.Builder>?) = apply { this.beforeSteps = beforeSteps }

        fun withAfterSteps(afterSteps: List<TestStep.Builder>?) = apply { this.afterSteps = afterSteps }

        fun withParent(parent: String) = apply { this.parent = parent }

        fun withPosition(position: Int) = apply { this.position = position }

        fun build(): List<TestScenario> {

            return data?.flatMapIndexed { i, dataTable ->
                tags = if (dataTable.first != null) dataTable.first!!.union(tags ?: emptySet()) else tags
                dataTable.second!!.mapIndexed { j, dataRow ->
                    val testScenarioName = ParametersUtils.replaceParametersIfFound(name!!, dataRow)
                    val path = "$parent:$testScenarioName:$i:$j"
                    val (beforeTestSteps, afterTestSteps) = buildBeforeAndAfterTestSteps(path)
                    TestScenario(
                        path, testScenarioName,
                        getTestStepsWithAppliedTestData(dataRow, path), tags, beforeTestSteps, afterTestSteps
                    )
                }.toList()
            }?.toList() ?: run {
                val path = "$parent:$name"
                val (beforeTestSteps, afterTestSteps) = buildBeforeAndAfterTestSteps(path)
                val steps = stepBuilders
                    .mapIndexed { i, builder ->
                        builder
                            .withParent(path)
                            .withPosition(i)
                            .build()
                    }
                    .toList()
                return listOf(TestScenario(path, name!!, steps, tags, beforeTestSteps, afterTestSteps))
            }
        }

        private fun buildBeforeAndAfterTestSteps(parentPath: String): Pair<List<TestStep>?, List<TestStep>?> {
            var beforeAfterStepsCounter = 0
            return Pair(
                beforeSteps?.map {
                    it.withParent(parentPath).withPosition(beforeAfterStepsCounter++).build()
                }?.toList(),
                afterSteps?.map {
                    it.withParent(parentPath).withPosition(beforeAfterStepsCounter++).build()
                }?.toList()
            )
        }

        private fun getTestStepsWithAppliedTestData(testDataRow: Map<String, String>, parentPath: String): List<TestStep> {
            return stepBuilders
                .mapIndexed { i, builder ->
                    builder
                        .withPosition(i)
                        .withParent(parentPath)
                        .buildAndApplyTestData(testDataRow)
                }
                .toList()
        }

    }

}