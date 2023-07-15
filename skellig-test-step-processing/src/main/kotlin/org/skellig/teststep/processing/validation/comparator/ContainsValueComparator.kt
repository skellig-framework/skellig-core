package org.skellig.teststep.processing.validation.comparator

class ContainsValueComparator : ValueComparator {

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        if (args.isNotEmpty() && actualValue != null) {
            return args.filterNotNull().map { it.toString() }.all {
                when {
                    actualValue::class == String::class -> (actualValue as String).contains(it)
                    actualValue.javaClass.isArray -> compareArray(actualValue, it)
                    actualValue is Collection<*> -> compareCollection(actualValue, it)
                    actualValue is Map<*, *> -> compareMap(actualValue, it)
                    else -> actualValue.toString().contains(it)
                }
            }
        }
        return false
    }

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

    override fun getName(): String = "contains"
}