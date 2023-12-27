package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SubStringValueExtractorTest {

    private val valueExtractor = SubStringValueExtractor()

    @Test
    fun testSubstringWithNullParams() {
        assertEquals("a/b/c", valueExtractor.execute("subString","a/b/c", arrayOf(null)))
    }

    @Test
    fun testSubstringWithoutParams() {
        assertEquals("a/b/c", valueExtractor.execute("subString","a/b/c", arrayOf("")))
    }

    @Test
    fun testSubstringWithNonExistentDelimiter() {
        assertEquals("a/b/c", valueExtractor.execute("subString","a/b/c", arrayOf(",")))
    }

    @Test
    fun testSubstringFirst() {
        assertEquals("b/c", valueExtractor.execute("subString", "a/b/c", arrayOf("/")))
    }
}