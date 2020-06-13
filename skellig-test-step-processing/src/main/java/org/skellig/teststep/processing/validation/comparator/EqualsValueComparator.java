package org.skellig.teststep.processing.validation.comparator;

public class EqualsValueComparator implements ValueComparator {

    @Override
    public boolean compare(Object actualValue, Object expectedValue) {
        return expectedValue == null || expectedValue.equals(actualValue);
    }

    @Override
    public boolean isApplicable(Object expectedValue) {
        return true;
    }
}
