package org.skellig.teststep.processing.validation.comparator;

public interface ValueComparator {

    boolean compare(Object expectedValue, Object actualValue);

    boolean isApplicable(Object expectedValue);
}