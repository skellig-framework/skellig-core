package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class DateTimeCompareFunctionExecutorTest {

    private val comparator = DateTimeCompareFunctionExecutor()

    @Test
    fun `compare date and time with different arguments`() {
        assertTrue(comparator.execute("between", LocalDateTime.now().plus(300, ChronoUnit.MILLIS), arrayOf("now", "1 second after")))
        assertTrue(comparator.execute("between", LocalDateTime.now(), arrayOf("2 seconds ago", "1 second after")))
        assertTrue(comparator.execute("between", LocalDateTime.now().plus(497, ChronoUnit.MILLIS), arrayOf("now", "500 milliseconds after")))
        assertTrue(comparator.execute("between", LocalDateTime.now().plus(1, ChronoUnit.MILLIS).toInstant(ZoneOffset.UTC), arrayOf("1 millisecond ago", "1 millisecond after")))
        assertTrue(comparator.execute("between", LocalDateTime.now().minusDays(4), arrayOf("5 days ago", "now")))
        assertTrue(comparator.execute("between", LocalDateTime.now().minusMinutes(4), arrayOf("5 minutes ago", "2 minutes ago")))
        assertTrue(comparator.execute("between", LocalDateTime.now().minusMinutes(32), arrayOf("1 hour ago", "30 minutes ago")))
        assertTrue(comparator.execute("between", LocalDateTime.now().minusHours(23), arrayOf("yesterday", "now")))
        assertTrue(comparator.execute("between", LocalDateTime.now().plusHours(23), arrayOf("now", "tomorrow")))
        assertTrue(comparator.execute("between", java.sql.Date.valueOf(LocalDate.now()), arrayOf("now", "now")))
        assertTrue(comparator.execute("between", LocalDate.now().plusDays(1), arrayOf("yesterday", "tomorrow")))
        assertTrue(comparator.execute("between", LocalDate.of(2020, 12, 10), arrayOf("01/12/2020", "01/01/2021", "dd/MM/yyyy")))
        assertTrue(comparator.execute("between", LocalDate.of(2020, 12, 10), arrayOf(LocalDate.of(2020, 12, 1), LocalDate.of(2021, 1, 1))))
        assertTrue(comparator.execute("between", "31/12/2020", arrayOf("01/12/2020", "01/01/2021", "dd/MM/yyyy")))
        assertTrue(
            comparator.execute(
                "between", LocalDateTime.of(2020, 6, 5, 10, 30, 45), arrayOf("05.06.2020 10:30:00", "05.06.2020 10:30:50", "dd.MM.yyyy HH:mm:ss")
            )
        )
        assertTrue(
            comparator.execute(
                "between", LocalDateTime.of(2020, 6, 5, 10, 30, 45), arrayOf(LocalDateTime.of(2020, 6, 5, 10, 30, 0), LocalDateTime.of(2020, 6, 5, 10, 30, 50))
            )
        )
        assertTrue(
            comparator.execute(
                "between", "05.06.2020 10:30:00", arrayOf("05.06.2020 10:30:00", "05.06.2020 10:30:50", "dd.MM.yyyy HH:mm:ss", "+10")
            )
        )
        assertTrue(comparator.execute("between", java.sql.Date.valueOf(LocalDate.now()), arrayOf("yesterday", "tomorrow")))
        assertTrue(comparator.execute("between", Timestamp.valueOf(LocalDateTime.now()), arrayOf("yesterday", "tomorrow")))
        assertTrue(comparator.execute("between", Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)), arrayOf("yesterday", "tomorrow")))
        assertTrue(comparator.execute("between", ZonedDateTime.now(), arrayOf("yesterday", "tomorrow")))
        assertTrue(comparator.execute("between", LocalDateTime.now().toInstant(ZoneOffset.UTC), arrayOf("yesterday", "tomorrow")))

        assertFalse(comparator.execute("between", LocalDateTime.now().plus(300, ChronoUnit.MILLIS), arrayOf("2 seconds after", "10 seconds after")))
        assertFalse(comparator.execute("between", LocalDate.now().plusDays(2), arrayOf("yesterday", "tomorrow")))
        assertFalse(
            comparator.execute(
                "between", LocalDateTime.of(2020, 6, 5, 20, 10, 0), arrayOf("05.06.2020 08:30:00", "05.06.2020 15:30:50", "dd.MM.yyyy HH:mm:ss")
            )
        )
        assertFalse(
            comparator.execute(
                "between", LocalDateTime.of(2020, 6, 5, 10, 10, 0), arrayOf("05.06.2020 08:30:00", "05.06.2020 15:30:50", "dd.MM.yyyy HH:mm:ss", "+10")
            )
        )
        assertFalse(
            comparator.execute(
                "between", LocalDateTime.of(2020, 6, 5, 10, 10, 0),
                arrayOf(LocalDateTime.of(2020, 6, 5, 8, 30, 0).toInstant(ZoneOffset.of("+10")), LocalDateTime.of(2020, 6, 5, 15, 30, 50).toInstant(ZoneOffset.of("+10")))
            )
        )
    }

    @Test
    fun `compare date with invalid arguments`() {
        assertThrows<ValidationException> { comparator.execute("between", LocalDateTime.now(), arrayOf("", "1 second after")) }
        assertFalse(comparator.execute("between", LocalDateTime.now().plusSeconds(1), arrayOf("now", "now")))
        assertThrows<ValidationException> { comparator.execute("between", LocalDateTime.now(), arrayOf("now", "")) }
        assertThrows<ValidationException> { comparator.execute("between", LocalDateTime.now(), arrayOf("55", "now")) }
        assertThrows<ValidationException> { comparator.execute("between", LocalDateTime.now(), arrayOf("now", "ggg")) }
        assertThrows<ValidationException> { comparator.execute("between", LocalDateTime.now(), arrayOf("now", "12/10/2020")) } // no format provided
        assertThrows<ValidationException> { comparator.execute("between", 100, arrayOf("now", "now")) }
    }

    @Test
    fun `compare date and time with invalid type of arguments`() {
        val ex = assertThrows<FunctionExecutionException> { comparator.execute("between", LocalDateTime.now(), arrayOf(0, 1)) }
        assertEquals("Failed to execute function 'between'. Invalid argument type 'Integer'. Expected: String, LocalDateTime, LocalDate or Instant", ex.message)
    }
}