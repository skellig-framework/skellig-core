package org.skellig.teststep.processing.valueextractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValueExtractionException
import java.time.LocalDate
import java.time.LocalDateTime

class BaseToDateTimeValueExtractorTest {

    private val dateTimeExtractor = ToDateTimeValueExtractor()
    private val dateExtractor = ToDateValueExtractor()

    @Test
    fun testToDate() {
        assertEquals(LocalDate.of(2025, 4, 10), dateExtractor.extractFrom("toDate", "10-04-2025", arrayOf("dd-MM-yyyy")))
        assertEquals(LocalDate.of(1995, 11, 21), dateExtractor.extractFrom("toDate", "21.11.1995", arrayOf("dd.MM.yyyy ")))
        assertEquals(LocalDate.of(2007, 7, 8), dateExtractor.extractFrom("toDate", "08/07/2007", arrayOf(" dd/MM/yyyy ")))
        assertEquals(LocalDate.of(2012, 12, 9), dateExtractor.extractFrom("toDate", "09/12/2012", arrayOf("dd/MM/yyyy", "+15"))) // ignore timezone
    }

    @Test
    fun testToDateTime() {
        assertEquals(
            LocalDateTime.of(2025, 4, 10, 15, 50, 31),
            dateTimeExtractor.extractFrom("toDateTime", "10-04-25T15:50:31", arrayOf("dd-MM-yy'T'HH:mm:ss"))
        )
        assertEquals(LocalDateTime.of(1995, 11, 21, 5, 15, 0), dateTimeExtractor.extractFrom("toDateTime", "21.11.1995 05:15", arrayOf("dd.MM.yyyy HH:mm")))
        assertEquals(
            LocalDateTime.of(2007, 7, 8, 10, 0, 15, 300_000000),
            dateTimeExtractor.extractFrom("toDateTime", "08/07/2007 10-00-15.300", arrayOf(" dd/MM/yyyy HH-mm-ss.SSS"))
        )
        assertEquals(
            LocalDateTime.of(2012, 12, 9, 7, 30, 0),
            dateTimeExtractor.extractFrom("toDateTime", "09/12/2012 22:30", arrayOf("dd/MM/yyyy HH:mm", "+15"))
        )
        assertEquals(
            LocalDateTime.of(2012, 12, 10, 4, 45, 0),
            dateTimeExtractor.extractFrom("toDateTime", "09/12/2012 23:45", arrayOf("dd/MM/yyyy HH:mm", "-5"))
        )
    }

    @Test
    fun testInvalidParams() {
        assertNull(dateExtractor.extractFrom("toDate", null, arrayOf("dd/MM/yyyy")))
        assertThrows<ValueExtractionException> { dateExtractor.extractFrom("toDate", "", arrayOf("dd/MM/yyyy")) }
        assertThrows<ValueExtractionException> { dateExtractor.extractFrom("toDate", "something", arrayOf("dd/MM/yyyy")) }
        assertThrows<ValueExtractionException> { dateExtractor.extractFrom("toDate", "17/90/2020", arrayOf("dd/MM/yyyy")) }
        assertThrows<ValueExtractionException> { dateTimeExtractor.extractFrom("toDateTime", "14/12/2020 90:30", arrayOf("dd/MM/yyyy HH:mm")) }
    }
}