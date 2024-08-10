package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class ToInstantFunctionExecutorTest {

    private var toInstantFunctionExecutor = ToInstantFunctionExecutor()

    @Test
    fun `convert to Instant with UTC timezone`() {
        val localDateTime = LocalDateTime.of(2023, 3, 1, 12, 0)
        val instant = toInstantFunctionExecutor.execute("toInstant", localDateTime, emptyArray())

        assertEquals(instant, Instant.ofEpochSecond(localDateTime.toEpochSecond(ZoneOffset.UTC)))
    }

    @Test
    fun `convert to Instant with 2 hours timezone`() {
        val localDateTime = LocalDateTime.of(2023, 3, 1, 12, 0)
        val zoneOffset = "+2"
        val instant = toInstantFunctionExecutor.execute("toInstant", localDateTime, arrayOf(zoneOffset))

        assertEquals(instant, Instant.ofEpochSecond(localDateTime.toEpochSecond(ZoneOffset.of(zoneOffset))))
    }

    @Test
    fun `convert to Instant an invalid value type`() {
        val stringValue = "not a LocalDateTime"
        val exception = assertThrows<FunctionExecutionException> {
            toInstantFunctionExecutor.execute("toInstant", stringValue, emptyArray())
        }

        assertEquals(exception.message, "Function `toInstant` can only be called on LocalDateTime value. Found String")
    }

    @Test
    fun `convert to Instant with UTC timezone and truncate to millis`() {
        val localDateTime = LocalDateTime.now()
        val args = arrayOf<Any?>("+0", "Millis")

        val expected = localDateTime
            .toInstant(ZoneOffset.of(args[0]!!.toString()))
            .truncatedTo(ChronoUnit.MILLIS)

        val actual = toInstantFunctionExecutor.execute("toInstant", localDateTime, args)

        assertEquals(expected, actual)
    }

    @Test
    fun `convert to Instant with UTC timezone and invalid truncate value`() {
        val localDateTime = LocalDateTime.now()

        val ex = assertThrows<IllegalStateException> {
            toInstantFunctionExecutor.execute("toInstant", localDateTime, arrayOf("+0", "invalid"))
        }

        assertEquals(ex.message, "Failed to truncate Instant with argument 'invalid' when calling function `toInstant`")

        val ex2 = assertThrows<IllegalStateException> {
            toInstantFunctionExecutor.execute("toInstant", localDateTime, arrayOf("+0", null))
        }

        assertEquals(ex2.message, "Failed to truncate Instant with argument 'null' when calling function `toInstant`")
    }

    @Test
    fun `test function name`() {
        assertEquals("toInstant", toInstantFunctionExecutor.getFunctionName())
    }
}