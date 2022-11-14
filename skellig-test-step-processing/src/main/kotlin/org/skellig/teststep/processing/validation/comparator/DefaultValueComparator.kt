package org.skellig.teststep.processing.validation.comparator

class DefaultValueComparator(private val comparators: MutableMap<String, ValueComparator>) : ValueComparator {

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        return comparators[comparator]?.compare(comparator, args, actualValue) ?: false
    }

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean {
        return false
    }

    override fun isApplicable(expectedValue: Any?): Boolean {
        return true
    }

    override fun getName(): String = ""

    class Builder {
        private val valueComparators: MutableMap<String, ValueComparator>

        init {
            valueComparators = mutableMapOf()
            withValueComparator(ContainsValueComparator())
            withValueComparator(MatchValueComparator())
            withValueComparator(NumericValueComparator())
            withValueComparator(DateTimeValueComparator())
        }

        fun withValueComparator(valueComparator: ValueComparator) = apply {
            valueComparators[valueComparator.getName()] = valueComparator
        }

        fun build(): ValueComparator {
            val defaultValueComparator = DefaultValueComparator(valueComparators)
            withValueComparator(NotValueComparator(defaultValueComparator))
            withValueComparator(EqualsValueComparator())
            return defaultValueComparator
        }
    }
}