package org.skellig.teststep.processing.value.function


/**
 * Executes the 'contains' function on a given value with [String] arguments.
 * The function can be applied to 'value' of:
 * - [String]
 * - [Array]
 * - [Collection]
 * - [Map]
 *
 * or otherwise the 'value' is converted to [String] and checks if it contains all items from the 'args'.
 *
 * Supported args:
 * - contains(`<array>`) - for example: 'contains(a, b, c)' checks if 'value' contains a, b and c letters and returns 'true' or 'false'.
 *
 */
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