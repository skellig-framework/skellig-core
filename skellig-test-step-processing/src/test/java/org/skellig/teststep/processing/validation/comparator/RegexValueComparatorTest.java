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
        assertTrue(regexValueComparator.compare("regex(\\w+\\s?=\\s?\\w+)", "a1 = v1"));
    }

    @Test
    void testRegexNotMatches() {
        assertFalse(regexValueComparator.compare("regex(\\d+\\s?=\\s?\\d+)", "a1 = v1"));
    }

    @Test
    void testRegexWithNullActualResult() {
        assertFalse(regexValueComparator.compare("regex(.*)", null));
    }

    @Test
    void testInvalidRegex() {
        assertFalse(regexValueComparator.compare("regex((??)", "value"));
    }
}