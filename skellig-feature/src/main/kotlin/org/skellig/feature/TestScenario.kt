package org.skellig.feature

open class TestScenario protected constructor(
    val name: String,
    val steps: List<TestStep>?,
    val tags: Set<String>?,
    val beforeSteps: List<TestStep>?,
    val afterSteps: List<TestStep>?
) : SkelligTestEntity {

    override fun getEntityName(): String = name

    override fun getEntityTags(): Set<String>? = tags

    class Builder {
        private var name: String? = null
        private var tags: Set<String>? = null
        private val stepBuilders = mutableListOf<TestStep.Builder>()
        private var data: MutableList<Pair<Set<String>?, MutableList<Map<String, String>>?>>? = null
        private var beforeSteps: List<TestStep>? = null
        private var afterSteps: List<TestStep>? = null

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

        fun withBeforeSteps(beforeSteps: List<TestStep>?) = apply { this.beforeSteps = beforeSteps }

        fun withAfterSteps(afterSteps: List<TestStep>?) = apply { this.afterSteps = afterSteps }

        fun build(): List<TestScenario> {
            return data?.flatMap { dataTable ->
                tags = if (dataTable.first != null) dataTable.first!!.union(tags ?: emptySet()) else tags
                dataTable.second!!.map { dataRow ->
                    TestScenario(
                        ParametersUtils.replaceParametersIfFound(name!!, dataRow),
                        getTestStepsWithAppliedTestData(dataRow), tags, beforeSteps, afterSteps
                    )
                }.toList()
            }?.toList() ?: run {
                val steps = stepBuilders.map { it.build() }.toList()
                return listOf(TestScenario(name!!, steps, tags, beforeSteps, afterSteps))
            }
        }

        private fun getTestStepsWithAppliedTestData(testDataRow: Map<String, String>): List<TestStep> {
            return stepBuilders
                .map { it.buildAndApplyTestData(testDataRow) }
                .toList()
        }

    }

}