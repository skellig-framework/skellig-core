package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.MatchValueComparator

class MatchValueComparatorTest {

    private var matchValueComparator: MatchValueComparator? = null

    @BeforeEach
    fun setUp() {
        matchValueComparator = MatchValueComparator()
    }

    @Test
    fun testRegexMatches() {
        Assertions.assertTrue(matchValueComparator!!.execute("match", "a1 = v1", arrayOf("\\w+\\s?=\\s?\\w+")))
    }

    @Test
    fun testRegexMatchesComplex() {
        Assertions.assertTrue(
            matchValueComparator!!.execute(
                "match", "{\"code\":\"cd00001\",\"values\":[\"a\",\"b\"]}", arrayOf(".*code\".*:.*\"cd00001\".*")
            )
        )
    }

    @Test
    fun testRegexNotMatches() {
        Assertions.assertFalse(matchValueComparator!!.execute("match", "a1 = v1", arrayOf("\\d+\\s?=\\s?\\d+")))
    }

    @Test
    fun testRegexWithNullActualResult() {
        Assertions.assertFalse(matchValueComparator!!.execute("match", null, arrayOf(". *")))
    }

    @Test
    fun testInvalidRegex() {
        Assertions.assertFalse(matchValueComparator!!.execute("match", "value", arrayOf("??")))
    }
}