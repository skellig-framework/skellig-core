package org.skellig.teststep.processing.validation.comparator

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.exception.ValidationException
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class DateTimeValueComparatorTest {

    private val comparator = DateTimeValueComparator()

    @Test
    fun testCompareDateAndTime() {
        assertTrue(comparator.compare("between", arrayOf("now", "1 second after"), LocalDateTime.now().plus(300, ChronoUnit.MILLIS)))
        assertTrue(comparator.compare("between", arrayOf("2 seconds ago", "1 second after"), LocalDateTime.now()))
        assertTrue(comparator.compare("between", arrayOf("now", "500 milliseconds after"), LocalDateTime.now().plus(497, ChronoUnit.MILLIS)))
        assertTrue(comparator.compare("between", arrayOf("5 days ago", "now"), LocalDateTime.now().minusDays(4)))
        assertTrue(comparator.compare("between", arrayOf("5 minutes ago", "2 minutes ago"), LocalDateTime.now().minusMinutes(4)))
        assertTrue(comparator.compare("between", arrayOf("1 hour ago", "30 minutes ago"), LocalDateTime.now().minusMinutes(32)))
        assertTrue(comparator.compare("between", arrayOf("yesterday", "now"), LocalDateTime.now().minusHours(23)))
        assertTrue(comparator.compare("between", arrayOf("now", "tomorrow"), LocalDateTime.now().plusHours(23)))
        assertTrue(comparator.compare("between", arrayOf("yesterday", "tomorrow"), LocalDate.now().plusDays(1)))
        assertTrue(comparator.compare("between", arrayOf("01/12/2020", "01/01/2021", "dd/MM/yyyy"), LocalDate.of(2020, 12, 10)))
        assertTrue(comparator.compare("between", arrayOf("01/12/2020", "01/01/2021", "dd/MM/yyyy"), "31/12/2020"))
        assertTrue(
            comparator.compare(
                "between", arrayOf("05.06.2020 10:30:00", "05.06.2020 10:30:50", "dd.MM.yyyy HH:mm:ss"),
                LocalDateTime.of(2020, 6, 5, 10, 30, 45)
            )
        )
        assertTrue(
            comparator.compare(
                "between", arrayOf("05.06.2020 10:30:00", "05.06.2020 10:30:50", "dd.MM.yyyy HH:mm:ss", "+10"),
                "05.06.2020 10:30:00"
            )
        )
        assertTrue(comparator.compare("between", arrayOf("yesterday", "tomorrow"), java.sql.Date.valueOf(LocalDate.now())))
        assertTrue(comparator.compare("between", arrayOf("yesterday", "tomorrow"), Timestamp.valueOf(LocalDateTime.now())))
        assertTrue(comparator.compare("between", arrayOf("yesterday", "tomorrow"), Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC))))
        assertTrue(comparator.compare("between", arrayOf("yesterday", "tomorrow"), ZonedDateTime.now()))
        assertTrue(comparator.compare("between", arrayOf("yesterday", "tomorrow"), LocalDateTime.now().toInstant(ZoneOffset.UTC)))

        assertFalse(comparator.compare("between", arrayOf("2 seconds after", "10 seconds after"), LocalDateTime.now().plus(300, ChronoUnit.MILLIS)))
        assertFalse(comparator.compare("between", arrayOf("yesterday", "tomorrow"), LocalDate.now().plusDays(2)))
        assertFalse(
            comparator.compare(
                "between", arrayOf("05.06.2020 08:30:00", "05.06.2020 15:30:50", "dd.MM.yyyy HH:mm:ss"),
                LocalDateTime.of(2020, 6, 5, 20, 10, 0)
            )
        )
        assertFalse(
            comparator.compare(
                "between", arrayOf("05.06.2020 08:30:00", "05.06.2020 15:30:50", "dd.MM.yyyy HH:mm:ss", "+10"),
                LocalDateTime.of(2020, 6, 5, 10, 10, 0)
            )
        )
    }

    @Test
    fun testCompareWithInvalidInput() {
        assertThrows<ValidationException> { comparator.compare("between", arrayOf("", "1 second after"), LocalDateTime.now()) }
        assertFalse(comparator.compare("between", arrayOf("now", "now"), LocalDateTime.now().plusSeconds(1)))
        assertThrows<ValidationException> { comparator.compare("between", arrayOf("now", ""), LocalDateTime.now()) }
        assertThrows<ValidationException> { comparator.compare("between", arrayOf("55", "now"), LocalDateTime.now()) }
        assertThrows<ValidationException> { comparator.compare("between", arrayOf("now", "ggg"), LocalDateTime.now()) }
        assertThrows<ValidationException> { comparator.compare("between", arrayOf("now", "12/10/2020"), LocalDateTime.now()) } // no format provided
        assertThrows<ValidationException> { comparator.compare("between", arrayOf("now", "now"), 100) }
    }
}