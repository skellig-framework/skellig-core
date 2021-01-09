package org.skellig.teststep.processing.validation.comparator

class EqualsValueComparator : ValueComparator {

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean {
        return expectedValue == actualValue
    }

    override fun isApplicable(expectedValue: Any?): Boolean {
        return true
    }
}