package org.skellig.teststep.processing.validation.comparator

interface ValueComparator {

    fun compare(expectedValue: Any, actualValue: Any?): Boolean

    fun isApplicable(expectedValue: Any): Boolean
}