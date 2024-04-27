package org.skellig.teststep.processing.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.lang.reflect.Method
import java.util.regex.Pattern

class ClassTestStepTest {

    @Test
    fun testToString() {
        val testStepMethod = mock<Method>()
        whenever(testStepMethod.name).thenReturn("testStepMethod")

        val toString = ClassTestStep(
            "test-id-1", Pattern.compile(".+"), this, testStepMethod, "Run something",
            mapOf(Pair("p1", "900"), Pair("p2", "none"))
        ).toString()

        assertEquals("test step instance = ClassTestStepTest\n" +
                "method name = testStepMethod\n" +
                "parameters {\n" +
                "  p1 = 900\n" +
                "  p2 = none\n" +
                "}\n", toString)
    }
}