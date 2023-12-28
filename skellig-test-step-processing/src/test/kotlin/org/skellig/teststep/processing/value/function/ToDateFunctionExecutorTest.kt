package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.time.LocalDate

class ToDateFunctionExecutorTest {

    private val converter = ToDateFunctionExecutor()

    @Test
    fun testConvertToDate() {
        val date = converter.execute("toDate", "05-12-2001", arrayOf("dd-MM-yyyy"))

        assertEquals(LocalDate.of(2001, 12, 5), date)
    }

    @Test
    fun testConvertToDateWithInvalidFormat() {
        val ex = assertThrows(FunctionExecutionException::class.java) { converter.execute("toDate", "77-77-2001", arrayOf("dd-MM-yyyy")) }

        assertEquals("Failed to convert date 77-77-2001 by pattern dd-MM-yyyy", ex.message)
    }

    @Test
    fun testConvertToDateWithInvalidNumberOfArguments() {
        val ex = assertThrows(FunctionExecutionException::class.java) { converter.execute("toDate", "01-01-2001", emptyArray()) }

        assertEquals("Function `toDate` can only accept 1 or 2 String arguments. Found 0", ex.message)
    }
}