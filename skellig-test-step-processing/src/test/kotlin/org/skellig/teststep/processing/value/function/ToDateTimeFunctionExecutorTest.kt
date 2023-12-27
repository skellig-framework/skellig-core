package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.function.ToDateTimeFunctionExecutor
import java.time.LocalDateTime

class ToDateTimeFunctionExecutorTest {

    val converter = ToDateTimeFunctionExecutor()

    @Test
    fun testConvertToDateTime() {
        val date = converter.execute("toDateTime", null, arrayOf("21-08-1995 10:30:00"))

        assertEquals(LocalDateTime.of(1995, 8, 21, 10, 30), date)
    }

}