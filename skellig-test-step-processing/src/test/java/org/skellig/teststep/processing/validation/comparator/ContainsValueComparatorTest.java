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
        assertTrue(containsValueComparator.compare("contains(value 1)", "test value 1"));
    }

    @Test
    void testNotContainsInString() {
        assertFalse(containsValueComparator.compare("contains(boo)", "test value 1"));
    }

    @Test
    void testContainsWhenActualIsNull() {
        assertFalse(containsValueComparator.compare("contains(value 1)", null));
    }

    @Test
    void testContainsWhenActualIsObject() {
        assertFalse(containsValueComparator.compare("contains(value 1)", new Object()));
    }

    @Test
    void testContainsWhenActualIsArray() {
        assertTrue(containsValueComparator.compare("contains(v1)", new String[]{"v1", "v2"}));
    }

    @Test
    void testContainsWhenActualIsArrayOfInteger() {
        assertTrue(containsValueComparator.compare("contains(2)", new Integer[]{1, 2, 3}));
    }
}