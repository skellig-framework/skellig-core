package org.skellig.teststep.processing.validation.comparator

class DefaultValueComparator(private val comparators: MutableMap<String, ValueComparator>) : ValueComparator {

    override fun compare(comparator: String, args: Array<Any?>, actualValue: Any?): Boolean {
        return comparators[comparator]?.compare(comparator, args, actualValue) ?: false
    }

    override fun getName(): String = ""

    class Builder {
        private val valueComparators: MutableMap<String, ValueComparator> = mutableMapOf()

        init {
            withValueComparator(ContainsValueComparator())
            withValueComparator(MatchValueComparator())
            withValueComparator(LessThanValueComparator())
            withValueComparator(LessOrEqualsValueComparator())
            withValueComparator(MoreThanValueComparator())
            withValueComparator(MoreOrEqualsThanComparator())
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