package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SubStringLastTestStepValueExtractorTest {

    private val valueExtractor = SubStringLastTestStepValueExtractor()

    @Test
    fun testSubstringWithEmptyParams() {
        assertEquals("c", valueExtractor.extract("a/b/c", ","))
    }

    @Test
    fun testSubstringLast() {
        assertEquals("c", valueExtractor.extract("a/b/c", "/"))
    }

    @Test
    fun testSubstringBetween() {
        assertEquals("c", valueExtractor.extract("a/b/c?i=1", "/,?"))
    }

    @Test
    fun testSubstringFromStart() {
        assertEquals("c", valueExtractor.extract("a/b/c", ",/"))
    }
}