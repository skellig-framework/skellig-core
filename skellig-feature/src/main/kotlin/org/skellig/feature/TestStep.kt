package org.skellig.feature

class TestStep private constructor(
    val path: String,
    val name: String,
    val parameters: Map<String, Any?>?
) : SkelligTestEntity {

    override fun getEntityName(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestStep

        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    class Builder {
        private var position = 0
        private var parent: String? = null
        private var name: String? = null
        private var parameters: MutableMap<String, String?>? = null

        fun withName(name: String) = apply { this.name = name.trim { it <= ' ' } }

        fun withParameters(parameters: MutableMap<String, String?>?) = apply { this.parameters = parameters }

        fun withParent(parent: String) = apply { this.parent = parent }

        fun withPosition(position: Int) = apply { this.position = position }

        fun build(): TestStep {
            return TestStep(createPath(), name!!, parameters)
        }

        fun buildAndApplyTestData(testData: Map<String, String>): TestStep {
            val newParameters = getParametersWithAppliedTestData(testData)
            return TestStep(createPath(), ParametersUtils.replaceParametersIfFound(name!!, testData), newParameters)
        }

        private fun createPath() = "$parent:$name:$position"

        private fun getParametersWithAppliedTestData(dataRow: Map<String, String>): Map<String, Any?>? {
            return parameters
                ?.map { it.key to it.value?.let { v -> ParametersUtils.replaceParametersIfFound(v, dataRow) } }
                ?.toMap()
        }
    }
}