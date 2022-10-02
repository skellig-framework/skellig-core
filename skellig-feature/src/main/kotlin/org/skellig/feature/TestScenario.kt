package org.skellig.feature

class TestScenario protected constructor(val name: String, val steps: List<TestStep>?, val tags: Set<String>?) {

    class Builder {
        private var name: String? = null
        private var tags: Set<String>? = null
        private val stepBuilders = mutableListOf<TestStep.Builder>()
        private var data: MutableList<Map<String, String>>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withStep(stepBuilder: TestStep.Builder) = apply { stepBuilders.add(stepBuilder) }

        /**
         * NOTE: tags are not supported yet.
         */
        fun withTags(tags: Set<String>?) = apply { this.tags = tags }

        fun withDataRow(dataRow: Map<String, String>) = apply {
            if (data == null) {
                data = mutableListOf()
            }
            data!!.add(dataRow)
        }

        fun build(): List<TestScenario> {
            return data?.let {
                return data!!.map {
                    TestScenario(ParametersUtils.replaceParametersIfFound(name!!, it),
                            getTestStepsWithAppliedTestData(it), tags)
                }.toList()
            } ?: run {
                val steps = stepBuilders.map { it.build() }.toList()
                return listOf(TestScenario(name!!, steps, tags))
            }
        }

        private fun getTestStepsWithAppliedTestData(testDataRow: Map<String, String>): List<TestStep> {
            return stepBuilders
                    .map { it.buildAndApplyTestData(testDataRow) }
                    .toList()
        }

    }
}