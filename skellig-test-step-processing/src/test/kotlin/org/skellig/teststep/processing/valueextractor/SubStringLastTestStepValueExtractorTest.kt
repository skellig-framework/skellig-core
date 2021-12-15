package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SubStringLastTestStepValueExtractorTest {

    private val valueExtractor = SubStringLastTestStepValueExtractor()

    @Test
    fun testSubstringLast() {
        assertEquals("c", valueExtractor.extract("a/b/c", "/"))
    }
}