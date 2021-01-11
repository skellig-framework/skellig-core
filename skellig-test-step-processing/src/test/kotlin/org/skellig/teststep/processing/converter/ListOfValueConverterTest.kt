package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ListOfValueConverterTest {

    private var valueConverter: ListOfValueConverter? = null

    @BeforeEach
    fun setUp() {
        valueConverter = ListOfValueConverter()
    }

    @Test
    fun testEmptyList() {
        assertTrue((valueConverter!!.convert("listOf()") as List<*>).isEmpty())
    }

    @Test
    fun testListWithOneElement() {
        assertTrue((valueConverter!!.convert("listOf(abc)") as List<*>).contains("abc"))
    }

    @Test
    fun testListWithFewElements() {
        val list = valueConverter!!.convert("listOf( a,b, c)") as List<*>

        assertAll(
                { assertTrue(list.contains("a")) },
                { assertTrue(list.contains("b")) },
                { assertTrue(list.contains("c")) }
        )
    }
}