package org.skellig.teststep.processing.validation.comparator;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultValueComparator implements ValueComparator {

    private final Collection<ValueComparator> comparators;

    private DefaultValueComparator(Collection<ValueComparator> comparators) {
        this.comparators = comparators;
    }

    @Override
    public boolean compare(Object actualValue, Object expectedValue) {
        return comparators.stream()
                .filter(comparator -> comparator.isApplicable(expectedValue))
                .anyMatch(comparator -> comparator.compare(actualValue, expectedValue));
    }

    @Override
    public boolean isApplicable(Object expectedValue) {
        return true;
    }

    public static class Builder {
        private Collection<ValueComparator> valueComparators;

        public Builder() {
            valueComparators = new ArrayList<>();
            withValueComparator(new ContainsValueComparator());
            withValueComparator(new RegexValueComparator());
        }

        public Builder withValueComparator(ValueComparator valueComparator) {
            valueComparators.add(valueComparator);
            return this;
        }

        public ValueComparator build() {
            withValueComparator(new EqualsValueComparator());
            return new DefaultValueComparator(valueComparators);
        }
    }
}

