package org.skellig.teststep.reader.validation.comparator;

public interface ValueComparator {

    boolean compare(Object actualValue, Object expectedValue);

    boolean isApplicable(Object expectedValue);
}