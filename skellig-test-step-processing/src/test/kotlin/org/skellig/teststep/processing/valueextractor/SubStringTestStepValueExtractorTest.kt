package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SubStringTestStepValueExtractorTest {

    private val valueExtractor = SubStringTestStepValueExtractor()

    @Test
    fun testSubstringWithNullParams() {
        assertEquals("a/b/c", valueExtractor.extractFrom("subString","a/b/c", arrayOf(null)))
    }

    @Test
    fun testSubstringWithoutParams() {
        assertEquals("a/b/c", valueExtractor.extractFrom("subString","a/b/c", arrayOf("")))
    }

    @Test
    fun testSubstringWithNonExistentDelimiter() {
        assertEquals("a/b/c", valueExtractor.extractFrom("subString","a/b/c", arrayOf(",")))
    }

    @Test
    fun testSubstringFirst() {
        assertEquals("b/c", valueExtractor.extractFrom("subString", "a/b/c", arrayOf("/")))
    }
}