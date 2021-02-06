package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ToDateTimeValueConverterTest {

    val converter = ToDateTimeValueConverter()

    @Test
    fun testConvertToDate() {
        val date = converter.convert("toDate(05-12-2001)")

        assertEquals(LocalDate.of(2001, 12, 5), date)
    }

    @Test
    fun testConvertToDateTime() {
        val date = converter.convert("toDateTime(21-08-1995 10:30:00)")

        assertEquals(LocalDateTime.of(1995, 8, 21, 10, 30), date)
    }

}