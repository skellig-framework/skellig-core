package org.skellig.feature

class TestStep protected constructor(val name: String, val parameters: Map<String, String?>?) {

    class Builder {
        private var name: String? = null
        private var parameters: MutableMap<String, String?>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withParameters(parameters: MutableMap<String, String?>) = apply { this.parameters = parameters }

        fun withParameter(name: String, value: String?) = apply {
            if (parameters == null) {
                parameters = mutableMapOf()
            }
            parameters!![name.trim { it <= ' ' }] = value?.trim { it <= ' ' }
        }

        fun build(): TestStep {
            return TestStep(name!!, parameters)
        }

        fun buildAndApplyTestData(testData: Map<String, String>): TestStep {
            val newParameters = getParametersWithAppliedTestData(testData)
            return TestStep(ParametersUtils.replaceParametersIfFound(name!!, testData), newParameters)
        }

        private fun getParametersWithAppliedTestData(dataRow: Map<String, String>): Map<String, String?>? {
            return parameters
                    ?.map { it.key to it.value?.let { v -> ParametersUtils.replaceParametersIfFound(v, dataRow)} }
                    ?.toMap()
        }
    }
}