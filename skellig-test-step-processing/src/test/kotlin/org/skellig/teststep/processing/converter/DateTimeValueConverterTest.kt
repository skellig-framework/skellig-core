package org.skellig.teststep.processing.converter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@DisplayName("Convert to current date")
class DateTimeValueConverterTest {

    private var dateTimeValueConverter: DateTimeValueConverter? = null

    @BeforeEach
    fun setUp() {
        dateTimeValueConverter = DateTimeValueConverter()
    }

    @Test
    @DisplayName("Then check current date is returned")
    fun testCurrentDateTime() {
        val expectedTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

        val dateTime = dateTimeValueConverter!!.convert("now()") as LocalDateTime?

        Assertions.assertEquals(expectedTime, dateTime!!.truncatedTo(ChronoUnit.MINUTES))
    }

    @Test
    @DisplayName("When calling several times Then check dates are different")
    @Throws(InterruptedException::class)
    fun testCallTwiceCurrentDateTime() {
        val dateTime = dateTimeValueConverter!!.convert("now()") as LocalDateTime?

        Thread.sleep(10)

        val sameDateTime = dateTimeValueConverter!!.convert("now()") as LocalDateTime?

        Assertions.assertNotEquals(dateTime, sameDateTime)
    }

    @Test
    @DisplayName("With provided timezone Then check date with correct timezone returned")
    fun testGetDateTimeWithTimezone() {
        val expectedTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MINUTES)

        val dateTime = dateTimeValueConverter!!.convert("now(UTC)") as LocalDateTime?

        Assertions.assertEquals(expectedTime, dateTime!!.truncatedTo(ChronoUnit.MINUTES))
    }

    @Test
    @DisplayName("With invalid timezone Then check date with default timezone returned")
    fun testGetDateTimeWithInvalidTimezone() {
        val expectedTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

        val dateTime = dateTimeValueConverter!!.convert("now(invalid)") as LocalDateTime?

        Assertions.assertEquals(expectedTime, dateTime!!.truncatedTo(ChronoUnit.MINUTES))
    }
}