package org.skellig.teststep.processing.validation.comparator

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NotValueComparatorTest {


    @Test
    fun testNotComparison() {
        val valueComparator = mock<ValueComparator>()
        whenever(valueComparator.compare("", arrayOf("response"), "response")).thenReturn(true)
        whenever(valueComparator.compare("", arrayOf("response"), "other response")).thenReturn(false)

        val notValueComparator = NotValueComparator(valueComparator)

        assertFalse(notValueComparator.compare("not", arrayOf("response"), "response"))
        assertTrue(notValueComparator.compare("not", arrayOf("response"), "other response"))
    }
}