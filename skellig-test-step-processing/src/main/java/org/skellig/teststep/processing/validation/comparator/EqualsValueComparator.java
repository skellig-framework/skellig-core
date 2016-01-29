package org.skellig.teststep.processing.validation.comparator;

import java.util.Objects;

public class EqualsValueComparator implements ValueComparator {

    @Override
    public boolean compare(Object actualValue, Object expectedValue) {
        return Objects.equals(actualValue, expectedValue);
    }

    @Override
    public boolean isApplicable(Object expectedValue) {
        return true;
    }
}
