package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.ToDateFunctionExecutor
import java.time.LocalDate

class ToDateFunctionExecutorTest {

    val converter = ToDateFunctionExecutor()

    @Test
    fun testConvertToDate() {
        val date = converter.execute("toDate", arrayOf("05-12-2001"))

        assertEquals(LocalDate.of(2001, 12, 5), date)
    }

}