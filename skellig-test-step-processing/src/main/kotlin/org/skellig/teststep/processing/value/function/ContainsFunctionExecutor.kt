package org.skellig.teststep.processing.value.function


class ContainsFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (args.isNotEmpty() && value != null) {
            return args.filterNotNull().map { it.toString() }.all {
                when {
                    value::class == String::class -> (value as String).contains(it)
                    value.javaClass.isArray -> compareArray(value, it)
                    value is Collection<*> -> compareCollection(value, it)
                    value is Map<*, *> -> compareMap(value, it)
                    else -> value.toString().contains(it)
                }
            }
        }
        return false
    }

    override fun getFunctionName(): String = "contains"

    private fun compareCollection(actualValue: Collection<*>, expectedValueAsString: String?) = actualValue
        .map { it.toString() }
        .any { it == expectedValueAsString }

    private fun compareMap(actualValue: Map<*, *>, expectedValueAsString: String?) =
        compareCollection(actualValue.values, expectedValueAsString)

    private fun compareArray(actualValue: Any?, expectedValueAsString: String?) =
        if (actualValue is ByteArray) {
            actualValue
                .map { it.toString() }
                .any { it == expectedValueAsString }
        } else {
            (actualValue as Array<*>)
                .map { it.toString() }
                .any { it == expectedValueAsString }
        }
}