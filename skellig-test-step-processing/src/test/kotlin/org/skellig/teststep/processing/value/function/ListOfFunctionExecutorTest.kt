package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ListOfFunctionExecutorTest {

    private var valueConverter: ListOfFunctionExecutor? = null

    @BeforeEach
    fun setUp() {
        valueConverter = ListOfFunctionExecutor()
    }

    @Test
    fun testEmptyList() {
        assertTrue((valueConverter!!.execute("listOf", null, emptyArray()) as List<*>).isEmpty())
    }

    @Test
    fun testListWithOneElement() {
        val elements = "abc"
        assertTrue((valueConverter!!.execute("listOf", null, arrayOf(elements)) as List<*>).contains(elements))
    }

    @Test
    fun testListWithOneElementCommaSeparated() {
        val elements = "a,b,c"
        assertTrue((valueConverter!!.execute("listOf", null, arrayOf(elements)) as List<*>).containsAll(elements.split(",").toList()))
    }

    @Test
    fun testListWithFewElements() {
        val list = valueConverter!!.execute("listOf", null, arrayOf("a", "b", "c")) as List<*>

        assertAll(
            { assertTrue(list.contains("a")) },
            { assertTrue(list.contains("b")) },
            { assertTrue(list.contains("c")) }
        )
    }
}