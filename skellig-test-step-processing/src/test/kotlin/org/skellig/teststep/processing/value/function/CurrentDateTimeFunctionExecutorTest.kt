package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

@DisplayName("Convert to current date")
class CurrentDateTimeFunctionExecutorTest {

    private var valueConverter = CurrentDateTimeFunctionExecutor()

    @Nested
    inner class NoFormatDateTime {

        @Test
        @DisplayName("Then check current date is returned")
        fun testCurrentDateTime() {
            val expectedTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

            val dateTime = valueConverter.execute("now", null, emptyArray()) as LocalDateTime?

            assertEquals(expectedTime, dateTime!!.truncatedTo(ChronoUnit.MINUTES))
        }

        @Test
        @DisplayName("When calling several times Then check dates are different")
        @Throws(InterruptedException::class)
        fun testCallTwiceCurrentDateTime() {
            val dateTime = valueConverter.execute("now", null, emptyArray()) as LocalDateTime?

            Thread.sleep(10)

            val sameDateTime = valueConverter.execute("now", null, emptyArray()) as LocalDateTime?

            Assertions.assertNotEquals(dateTime, sameDateTime)
        }

        @Test
        @DisplayName("With provided timezone Then check date with correct timezone returned")
        fun testGetDateTimeWithTimezone() {
            val expectedTime = LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.MINUTES)

            val dateTime = valueConverter.execute("now", null, arrayOf("UTC")) as LocalDateTime?

            assertEquals(expectedTime, dateTime!!.truncatedTo(ChronoUnit.MINUTES))
        }

        @Test
        @DisplayName("With invalid timezone Then check date with default timezone returned")
        fun testGetDateTimeWithInvalidTimezone() {
            val ex = Assertions.assertThrows(FunctionExecutionException::class.java)
            { valueConverter.execute("now", null, arrayOf("invalid")) as LocalDateTime? }

            assertEquals("Cannot get current date for the timezone 'invalid'", ex.message)
        }
    }

    @Nested
    @DisplayName("And format")
    inner class FormattedDateTime {

        @Test
        @DisplayName("When date pattern is invalid Then throw exception")
        fun testNoFormat() {
            assertNotNull(valueConverter.execute("now", null, arrayOf("", "")))
        }

        @Test
        @DisplayName("When date pattern is invalid Then throw exception")
        fun testInvalidFormat() {
            val formatPattern = "invalid"
            val ex = Assertions.assertThrows(FunctionExecutionException::class.java)
            { valueConverter.execute("now", null, arrayOf("", formatPattern)) }

            assertEquals("Cannot format current date with the format '$formatPattern'", ex.message)
        }

        @Test
        @DisplayName("When date pattern is yyyyMMdd")
        fun testFormatSimpleDate() {
            val formatPattern = "yyyyMMdd"

            val result = valueConverter.execute("now", null, arrayOf("", formatPattern))

            assertEquals(LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatPattern)), result)
        }

        @Test
        @DisplayName("When time pattern is hh:MM:ss")
        fun testFormatSimpleTime() {
            val formatPattern = "hh:MM:ss"

            val result = valueConverter.execute("now", null, arrayOf("", formatPattern))

            assertEquals(LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatPattern)), result)
        }

        @Test
        @DisplayName("When date pattern is dd/MM/yyyy'T'HH:mm")
        fun testFormatSimpleDateTime() {
            val formatPattern = "dd/MM/yyyy'T'HH:mm"

            val result = valueConverter.execute("now", null, arrayOf("", formatPattern))

            assertEquals(LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatPattern)), result)
        }

        @Test
        @DisplayName("When date pattern is yyyy.MM.dd'T'HH:mm:ss:SSS")
        fun testFormatSimpleDateTime2() {
            val formatPattern = "yyyy.MM.dd'T'HH:mm:ss:SSS"

            val result = valueConverter.execute("now", null, arrayOf("", formatPattern))
            val pattern = Pattern.compile("\\d{4}.\\d{2}.\\d{2}T\\d{2}:\\d{2}:\\d{2}:\\d+")
            val matcher = pattern.matcher(result.toString())

            Assertions.assertTrue(matcher.matches(), "Result didn't match the expected pattern: $pattern")
        }

        @Test
        @DisplayName("When date pattern is yyyy-MM-dd'T'00:00:00")
        fun testFormatSimpleDateWithFixedTime() {
            val formatPattern = "yyyy-MM-dd'T'00:00:00"

            val result = valueConverter.execute("now", null, arrayOf("", formatPattern))

            assertEquals(LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatPattern)), result)
        }

        @Test
        @DisplayName("When date pattern is yyyy-MM-dd'T'HH:mm with timezone UTC")
        fun testFormatSimpleDateTimeWithTimezone() {
            val formatPattern = "yyyy-MM-dd'T'HH:mm"
            val timezone = "UTC"

            val result = valueConverter.execute("now", null, arrayOf(timezone, formatPattern))

            assertEquals(LocalDateTime.now(ZoneId.of(timezone)).format(DateTimeFormatter.ofPattern(formatPattern)), result)
        }
    }
}