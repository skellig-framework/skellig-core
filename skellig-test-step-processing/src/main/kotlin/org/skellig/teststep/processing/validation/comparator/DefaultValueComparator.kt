package org.skellig.teststep.processing.validation.comparator

import java.util.*

class DefaultValueComparator(private val comparators: Collection<ValueComparator>) : ValueComparator {

    override fun compare(expectedValue: Any?, actualValue: Any?): Boolean {
        return comparators
                .filter { it.isApplicable(expectedValue) }
                .any { it.compare(expectedValue, actualValue) }
    }

    override fun isApplicable(expectedValue: Any?): Boolean {
        return true
    }

    class Builder {
        private val valueComparators: MutableCollection<ValueComparator>

        init {
            valueComparators = ArrayList()
            withValueComparator(ContainsValueComparator())
            withValueComparator(MatchValueComparator())
            withValueComparator(NumericValueComparator())
        }

        fun withValueComparator(valueComparator: ValueComparator) = apply {
            valueComparators.add(valueComparator)
        }

        fun build(): ValueComparator {
            val defaultValueComparator = DefaultValueComparator(valueComparators)
            withValueComparator(NotValueComparator(defaultValueComparator))
            withValueComparator(EqualsValueComparator())
            return defaultValueComparator
        }
    }
}