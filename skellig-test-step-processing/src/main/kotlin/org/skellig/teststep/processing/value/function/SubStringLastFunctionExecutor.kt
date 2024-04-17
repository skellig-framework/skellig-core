package org.skellig.teststep.processing.value.function

/**
 * Executes the 'subStringLast' function for a [String] value which returns a substring from last occurrence of a provided [String] in args.
 *
 * Supported args:
 * - subStringLast(`<text>`) - where `<text>` is the last occurrence of a [String] in 'value' where to start extraction of substring from.
 */
class SubStringLastFunctionExecutor : SubStringFunctionExecutor() {

    override fun subStringAfter(value: String, after: String): String {
        return value.substringAfterLast(after)
    }

    override fun getFunctionName(): String {
        return "subStringLast"
    }
}