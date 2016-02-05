package org.skellig.teststep.processing.validation.comparator;

import java.util.Objects;

class EqualsValueComparator implements ValueComparator {

    @Override
    public boolean compare(Object expectedValue, Object actualValue) {
        return Objects.equals(expectedValue, actualValue);
    }

    @Override
    public boolean isApplicable(Object expectedValue) {
        return true;
    }
}
