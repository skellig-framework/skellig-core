package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.extractor.SubStringLastValueExtractor

internal class SubStringLastValueExtractorTest {

    private val valueExtractor = SubStringLastValueExtractor()

    @Test
    fun testSubstringLast() {
        assertEquals("c", valueExtractor.extractFrom("subString", "a/b/c", arrayOf("/")))
    }
}