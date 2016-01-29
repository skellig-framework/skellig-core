package org.skellig.teststep.processing.validation.comparator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexValueComparatorTest {

    private RegexValueComparator regexValueComparator;

    @BeforeEach
    void setUp() {
        regexValueComparator = new RegexValueComparator();
    }

    @Test
    void testRegexMatches() {
        assertTrue(regexValueComparator.compare("a1 = v1", "regex(\\w+\\s?=\\s?\\w+)"));
    }

    @Test
    void testRegexNotMatches() {
        assertFalse(regexValueComparator.compare("a1 = v1", "regex(\\d+\\s?=\\s?\\d+)"));
    }

    @Test
    void testRegexWithNullActualResult() {
        assertFalse(regexValueComparator.compare(null, "regex(.*)"));
    }

    @Test
    void testInvalidRegex() {
        assertFalse(regexValueComparator.compare("value", "regex((??)"));
    }
}