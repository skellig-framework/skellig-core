package org.skellig.teststep.processing.validation.comparator

class NotValueComparator(private val valueComparator: ValueComparator) : ValueComparator {

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        if (actualValue != null && args.size == 1) {
           return !valueComparator.compare("", arrayOf(args[0]), actualValue)
        }
        return false
    }

    override fun getName(): String = "not"
}