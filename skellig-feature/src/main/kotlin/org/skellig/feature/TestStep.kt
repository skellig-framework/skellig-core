package org.skellig.feature

class TestStep private constructor(val name: String, val parameters: Map<String, String?>?, val tags: Set<String>?) {

    class Builder {
        private var name: String? = null
        private var tags: Set<String>? = null
        private var parameters: MutableMap<String, String?>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withTags(tags: Set<String>?) = apply { this.tags = tags }

        fun withParameters(parameters: MutableMap<String, String?>) = apply { this.parameters = parameters }

        fun withParameter(name: String, value: String?) = apply {
            if (parameters == null) {
                parameters = mutableMapOf()
            }
            parameters!![name.trim { it <= ' ' }] = value?.trim { it <= ' ' }
        }

        fun build(): TestStep {
            return TestStep(name!!, parameters, tags)
        }

        fun buildAndApplyTestData(testData: Map<String, String>): TestStep {
            val newParameters = getParametersWithAppliedTestData(testData)
            return TestStep(ParametersUtils.replaceParametersIfFound(name!!, testData), newParameters, tags)
        }

        private fun getParametersWithAppliedTestData(dataRow: Map<String, String>): Map<String, String?>? {
            return parameters
                ?.map { it.key to it.value?.let { v -> ParametersUtils.replaceParametersIfFound(v, dataRow) } }
                ?.toMap()
        }
    }
}