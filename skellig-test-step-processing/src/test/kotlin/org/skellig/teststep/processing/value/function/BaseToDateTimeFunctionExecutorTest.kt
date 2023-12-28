package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.LocalDate
import java.time.LocalDateTime

class BaseToDateTimeFunctionExecutorTest {

    private val dateTimeFunctionExecutor = ToDateTimeFunctionExecutor()
    private val dateFunctionExecutor = ToDateFunctionExecutor()

    @Test
    fun testToDate() {
        assertEquals(LocalDate.of(2025, 4, 10), dateFunctionExecutor.execute("toDate", "10-04-2025", arrayOf("dd-MM-yyyy")))
        assertEquals(LocalDate.of(1995, 11, 21), dateFunctionExecutor.execute("toDate", "21.11.1995", arrayOf("dd.MM.yyyy ")))
        assertEquals(LocalDate.of(2007, 7, 8), dateFunctionExecutor.execute("toDate", "08/07/2007", arrayOf(" dd/MM/yyyy ")))
        assertEquals(LocalDate.of(2012, 12, 9), dateFunctionExecutor.execute("toDate", "09/12/2012", arrayOf("dd/MM/yyyy", "+15"))) // ignore timezone
    }

    @Test
    fun testToDateTime() {
        assertEquals(
            LocalDateTime.of(2025, 4, 10, 15, 50, 31),
            dateTimeFunctionExecutor.execute("toDateTime", "10-04-25T15:50:31", arrayOf("dd-MM-yy'T'HH:mm:ss"))
        )
        assertEquals(LocalDateTime.of(1995, 11, 21, 5, 15, 0), dateTimeFunctionExecutor.execute("toDateTime", "21.11.1995 05:15", arrayOf("dd.MM.yyyy HH:mm")))
        assertEquals(
            LocalDateTime.of(2007, 7, 8, 10, 0, 15, 300_000000),
            dateTimeFunctionExecutor.execute("toDateTime", "08/07/2007 10-00-15.300", arrayOf(" dd/MM/yyyy HH-mm-ss.SSS"))
        )
        assertEquals(
            LocalDateTime.of(2012, 12, 9, 7, 30, 0),
            dateTimeFunctionExecutor.execute("toDateTime", "09/12/2012 22:30", arrayOf("dd/MM/yyyy HH:mm", "+15"))
        )
        assertEquals(
            LocalDateTime.of(2012, 12, 10, 4, 45, 0),
            dateTimeFunctionExecutor.execute("toDateTime", "09/12/2012 23:45", arrayOf("dd/MM/yyyy HH:mm", "-5"))
        )
    }

    @Test
    fun testInvalidParams() {
        assertNull(dateFunctionExecutor.execute("toDate", null, arrayOf("dd/MM/yyyy")))
        assertThrows<FunctionExecutionException> { dateFunctionExecutor.execute("toDate", "", arrayOf("dd/MM/yyyy")) }
        assertThrows<FunctionExecutionException> { dateFunctionExecutor.execute("toDate", "something", arrayOf("dd/MM/yyyy")) }
        assertThrows<FunctionExecutionException> { dateFunctionExecutor.execute("toDate", "17/90/2020", arrayOf("dd/MM/yyyy")) }
        assertThrows<FunctionExecutionException> { dateTimeFunctionExecutor.execute("toDateTime", "14/12/2020 90:30", arrayOf("dd/MM/yyyy HH:mm")) }
    }
}