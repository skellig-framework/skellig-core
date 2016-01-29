package org.skellig.teststep.processing.validation.comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainsValueComparatorTest {

    private ContainsValueComparator containsValueComparator;

    @BeforeEach
    void setUp() {
        containsValueComparator = new ContainsValueComparator();
    }

    @Test
    void testContainsInString() {
        assertTrue(containsValueComparator.compare("test value 1", "contains(value 1)"));
    }

    @Test
    void testNotContainsInString() {
        assertFalse(containsValueComparator.compare("test value 1", "contains(boo)"));
    }

    @Test
    void testContainsWhenActualIsNull() {
        assertFalse(containsValueComparator.compare(null, "contains(value 1)"));
    }

    @Test
    void testContainsWhenActualIsObject() {
        assertFalse(containsValueComparator.compare(new Object(), "contains(value 1)"));
    }

    @Test
    void testContainsWhenActualIsArray() {
        assertTrue(containsValueComparator.compare(new String[]{"v1", "v2"}, "contains(v1)"));
    }

    @Test
    void testContainsWhenActualIsArrayOfInteger() {
        assertTrue(containsValueComparator.compare(new Integer[]{1, 2, 3}, "contains(2)"));
    }
}