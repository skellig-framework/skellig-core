package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ToDateTimeValueConverterTest {

    val converter = ToDateTimeValueConverter()

    @Test
    fun testConvertToDateTime() {
        val date = converter.execute("toDateTime", arrayOf("21-08-1995 10:30:00"))

        assertEquals(LocalDateTime.of(1995, 8, 21, 10, 30), date)
    }

}