package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SubStringLastValueExtractorTest {

    private val valueExtractor = SubStringLastValueExtractor()

    @Test
    fun testSubstringLast() {
        assertEquals("c", valueExtractor.execute("subString", "a/b/c", arrayOf("/")))
    }
}