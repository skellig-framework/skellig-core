package org.skellig.teststep.processing.validation.comparator

/**
 * Compares actual and expected values using a specific comparison logic.
 */
interface ValueComparator {

    /**
     * Compare the actual value with expected one from args of the comparator.
     */
    fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean

    /**
     * Get name of the comparator, which will be registered globally.
     * This name can be used with the same syntax as ordinary functions,
     * ex: `compare(args...)`
     */
    fun getName(): String

    /**
     * Check if the comparator matches with the provided name.
     * This function can be used to differentiate comparators and ordinary functions.
     */
    fun isApplicable(comparatorName: String): Boolean = getName() == comparatorName
}