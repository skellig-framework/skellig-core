package org.skellig.teststep.processing.validation.comparator

class EqualsValueComparator : ValueComparator {

    companion object {
        private const val NULL = "null"
    }

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        return if (args.size == 1) {
            if (NULL == args[0]) actualValue == null
            else args[0] == actualValue
        } else false
    }

    override fun getName(): String = ""
}