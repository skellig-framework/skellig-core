package org.skellig.teststep.processing.validation.comparator

/**
 * Compares actual and expected values
 */
interface ValueComparator {

    /**
     * Compare the actual value with the value extracted from `expectedValue`.
     *
     * The `expectedValue` can be a simple value to expect, or a function (ex. func(expected value))
     */
    fun compare(expectedValue: Any?, actualValue: Any?): Boolean

    /**
     * Checks if this comparator is applicable for the expected value.
     * The applicability is checked against the `expectedValue` property, for example
     * if `expectedValue` is a correct function for this comparator.
     */
    fun isApplicable(expectedValue: Any?): Boolean
}