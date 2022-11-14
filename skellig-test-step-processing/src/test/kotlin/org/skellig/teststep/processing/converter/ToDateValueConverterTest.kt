package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ToDateValueConverterTest {

    val converter = ToDateValueConverter()

    @Test
    fun testConvertToDate() {
        val date = converter.execute("toDate", arrayOf("05-12-2001"))

        assertEquals(LocalDate.of(2001, 12, 5), date)
    }

}