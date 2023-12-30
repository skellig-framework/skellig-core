package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.MatchValueFunctionExecutor

class MatchValueFunctionExecutorTest {

    private var matchValueFunctionExecutor: MatchValueFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        matchValueFunctionExecutor = MatchValueFunctionExecutor()
    }

    @Test
    fun testRegexMatches() {
        Assertions.assertTrue(matchValueFunctionExecutor!!.execute("match", "a1 = v1", arrayOf("\\w+\\s?=\\s?\\w+")))
    }

    @Test
    fun testRegexMatchesComplex() {
        Assertions.assertTrue(
            matchValueFunctionExecutor!!.execute(
                "match", "{\"code\":\"cd00001\",\"values\":[\"a\",\"b\"]}", arrayOf(".*code\".*:.*\"cd00001\".*")
            )
        )
    }

    @Test
    fun testRegexNotMatches() {
        Assertions.assertFalse(matchValueFunctionExecutor!!.execute("match", "a1 = v1", arrayOf("\\d+\\s?=\\s?\\d+")))
    }

    @Test
    fun testRegexWithNullActualResult() {
        Assertions.assertFalse(matchValueFunctionExecutor!!.execute("match", null, arrayOf(". *")))
    }

    @Test
    fun testInvalidRegex() {
        Assertions.assertFalse(matchValueFunctionExecutor!!.execute("match", "value", arrayOf("??")))
    }
}