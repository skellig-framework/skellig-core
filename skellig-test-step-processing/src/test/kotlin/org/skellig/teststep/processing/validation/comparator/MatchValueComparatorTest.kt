package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MatchValueComparatorTest {

    private var matchValueComparator: MatchValueComparator? = null

    @BeforeEach
    fun setUp() {
        matchValueComparator = MatchValueComparator()
    }

    @Test
    fun testRegexMatches() {
        Assertions.assertTrue(matchValueComparator!!.compare("match", arrayOf("\\w+\\s?=\\s?\\w+"), "a1 = v1"))
    }

    @Test
    fun testRegexMatchesComplex() {
        Assertions.assertTrue(
            matchValueComparator!!.compare(
                "match", arrayOf(".*code\".*:.*\"cd00001\".*"),
                "{\"code\":\"cd00001\",\"values\":[\"a\",\"b\"]}"
            )
        )
    }

    @Test
    fun testRegexNotMatches() {
        Assertions.assertFalse(matchValueComparator!!.compare("match", arrayOf("\\d+\\s?=\\s?\\d+"), "a1 = v1"))
    }

    @Test
    fun testRegexWithNullActualResult() {
        Assertions.assertFalse(matchValueComparator!!.compare("match", arrayOf(". *"), null))
    }

    @Test
    fun testInvalidRegex() {
        Assertions.assertFalse(matchValueComparator!!.compare("match", arrayOf("??"), "value"))
    }
}