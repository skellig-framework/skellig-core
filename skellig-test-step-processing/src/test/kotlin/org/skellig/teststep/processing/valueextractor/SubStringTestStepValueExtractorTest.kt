package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SubStringTestStepValueExtractorTest {

    private val valueExtractor = SubStringTestStepValueExtractor()

    @Test
    fun testSubstringWithNullParams() {
        assertEquals("a/b/c", valueExtractor.extract("a/b/c", null))
    }

    @Test
    fun testSubstringWithoutParams() {
        assertEquals("a/b/c", valueExtractor.extract("a/b/c", ""))
    }

    @Test
    fun testSubstringWithEmptyParams() {
        assertEquals("a/b/c", valueExtractor.extract("a/b/c", ","))
    }

    @Test
    fun testSubstringFirst() {
        assertEquals("b/c", valueExtractor.extract("a/b/c", "/"))
    }

    @Test
    fun testSubstringBetween() {
        assertEquals("b", valueExtractor.extract("a/b/c", "/,/"))
    }

    @Test
    fun testSubstringFromStart() {
        assertEquals("a", valueExtractor.extract("a/b/c", ",/"))
    }
}