package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.value.function.exception.FunctionValueExecutionException
import java.time.LocalDate

class ToDateFunctionExecutorTest {

    private val converter = ToDateFunctionExecutor()

    @Test
    fun testConvertToDate() {
        val date = converter.execute("toDate", arrayOf("05-12-2001"))

        assertEquals(LocalDate.of(2001, 12, 5), date)
    }

    @Test
    fun testConvertToDateWithInvalidFormat() {
        val ex = assertThrows(TestValueConversionException::class.java) { converter.execute("toDate", arrayOf("77-77-2001")) }

        assertEquals("Failed to convert date 77-77-2001 by pattern dd-MM-yyyy", ex.message)
    }

    @Test
    fun testConvertToDateWithInvalidNumberOfArguments() {
        val ex = assertThrows(TestValueConversionException::class.java) { converter.execute("toDate", arrayOf("01-01-2001", "UTC")) }

        assertEquals("Function `toDate` can only accept 1 argument. Found 2", ex.message)
    }
}