package org.skellig.teststep.processing.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.PropertyValueExpression

class DefaultTestStepTest {

    @Test
    fun testToString() {
        val testStep = DefaultTestStep(
            id = "id",
            name = "name",
            execution = TestStepExecutionType.ASYNC,
            delay = 10,
            attempts = 2,
            timeout = 5,
            values = mutableMapOf("key1" to "value1"),
            // use suitable placeholders on parameters not used in the example
            scenarioStateUpdaters = listOf(
                ScenarioStateUpdater(PropertyValueExpression("p1"), AlphanumericValueExpression("abc"), emptyMap(), mock()),
                ScenarioStateUpdater(PropertyValueExpression("p2"), AlphanumericValueExpression("12345"), emptyMap(), mock()),
            ),
            validationDetails = null,
            testData = null
        )

        assertEquals(
            "id = id\n" +
                    "name = name\n" +
                    "execution = ASYNC\n" +
                    "timeout = 5\n" +
                    "delay = 10\n" +
                    "attempts = 2\n" +
                    "values {\n" +
                    "  key1 = value1\n" +
                    "}\n" +
                    "state {\n" +
                    "\${p1} = abc\n" +
                    "\${p2} = 12345\n" +
                    "}\n", testStep.toString()
        )
    }
}