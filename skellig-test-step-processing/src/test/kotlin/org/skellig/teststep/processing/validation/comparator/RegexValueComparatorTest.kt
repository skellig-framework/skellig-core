package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegexValueComparatorTest {

    private var regexValueComparator: RegexValueComparator? = null

    @BeforeEach
    fun setUp() {
        regexValueComparator = RegexValueComparator()
    }

    @Test
    fun testRegexMatches() {
        Assertions.assertTrue(regexValueComparator!!.compare("regex(\\w+\\s?=\\s?\\w+)", "a1 = v1"))
    }

    @Test
    fun testRegexNotMatches() {
        Assertions.assertFalse(regexValueComparator!!.compare("regex(\\d+\\s?=\\s?\\d+)", "a1 = v1"))
    }

    @Test
    fun testRegexWithNullActualResult() {
        Assertions.assertFalse(regexValueComparator!!.compare("regex(.*)", null))
    }

    @Test
    fun testInvalidRegex() {
        Assertions.assertFalse(regexValueComparator!!.compare("regex((??)", "value"))
    }
}