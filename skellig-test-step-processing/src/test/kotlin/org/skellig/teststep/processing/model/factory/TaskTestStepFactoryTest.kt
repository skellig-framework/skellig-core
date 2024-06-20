package org.skellig.teststep.processing.model.factory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.mockito.Mockito.mock
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression

class TaskTestStepFactoryTest {

    @Test
    fun testCreate() {
        val taskTestStepFactory = TaskTestStepFactory(mock(), mock())
        val taskValue = MapValueExpression(emptyMap())
        val parameters = mapOf(Pair("a", "b"))
        val testStep = taskTestStepFactory.create(
            "test 1",
            mapOf(
                Pair(
                    AlphanumericValueExpression("task"),
                    taskValue
                )
            ), parameters
        )

        assertAll(
            { assertEquals("test 1", testStep.name) },
            { assertEquals(taskValue, testStep.getTask()) },
            { assertEquals(parameters, testStep.parameters) }
        )
    }
}