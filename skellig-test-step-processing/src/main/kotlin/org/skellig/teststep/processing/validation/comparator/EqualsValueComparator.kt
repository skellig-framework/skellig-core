package org.skellig.teststep.processing.validation.comparator

class EqualsValueComparator : ValueComparator {

    companion object {
        private const val NULL = "null"
    }

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean {
        return if (NULL == expectedValue) actualValue == null
        else expectedValue == actualValue
    }

    override fun isApplicable(expectedValue: Any?): Boolean {
        return true
    }
}