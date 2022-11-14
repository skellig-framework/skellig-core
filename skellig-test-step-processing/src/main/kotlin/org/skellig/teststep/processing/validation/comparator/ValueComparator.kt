package org.skellig.teststep.processing.validation.comparator

/**
 * Compares actual and expected values using comparator
 */
interface ValueComparator {

    /**
     * Compare the actual value with expected one from args of the comparator.
     */
    fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean

    fun getName(): String
}