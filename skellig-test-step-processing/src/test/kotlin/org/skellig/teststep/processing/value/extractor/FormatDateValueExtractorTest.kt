package org.skellig.teststep.processing.value.extractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.time.LocalDate
import java.time.LocalDateTime

class FormatDateValueExtractorTest {


    @Test
    fun testFormatDate() {
        val valueExtractor = FormatDateValueExtractor()

        assertEquals(
            "2010-01-02", valueExtractor.extractFrom(
                "", LocalDate.of(2010, 1, 2),
                arrayOf("yyyy-MM-dd")
            )
        )
        assertEquals(
            "05.12.2015", valueExtractor.extractFrom(
                "", LocalDate.of(2015, 12, 5),
                arrayOf("dd.MM.yyyy")
            )
        )
    }

    @Test
    fun testFormatDateTime() {
        val valueExtractor = FormatDateTimeValueExtractor()

        val result = valueExtractor.extractFrom(
            "",
            LocalDateTime.of(2010, 6, 24, 17, 45, 38),
            arrayOf("yyyy-MM-dd HH:mm:ss")
        )

        assertEquals("2010-06-24 17:45:38", result)
    }

    @Test
    fun testFormatDateTimeForInvalidType() {
        val valueExtractor = FormatDateTimeValueExtractor()

        val ex = Assertions.assertThrows(FunctionValueExecutionException::class.java) {
            valueExtractor.extractFrom("", "2010-06-24 17:45:38", arrayOf("yyyy-MM-dd HH:mm:ss"))
        }

        assertEquals("Invalid type for 'formatDateTime' - String. Must be accessible from TemporalAccessor (ex. LocalDateTime)", ex.message)
    }
}