package org.skellig.teststep.processing.value.function

import org.junit.jupiter.api.Assertions.*

import org.apache.commons.lang3.SerializationUtils
import org.junit.jupiter.api.Test
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.io.Serializable
import java.nio.charset.StandardCharsets

class ToBytesFunctionExecutorTest {

    private val toBytesFunctionExecutor = ToBytesFunctionExecutor()

    @Test
    fun `convert serializable object to bytes`() {
        class SerializableObject : Serializable {
            val a = 1
            val b = "2"
        }
        val value = SerializableObject()
        val result = toBytesFunctionExecutor.execute("toBytes", value, emptyArray())

        assertArrayEquals(SerializationUtils.serialize(value), result as ByteArray)
    }

    @Test
    fun `convert string to bytes`() {
        val value = "a string"
        val result = toBytesFunctionExecutor.execute("toBytes", value, emptyArray())

        assertArrayEquals(value.toByteArray(StandardCharsets.UTF_8), result as ByteArray)
    }

    @Test
    fun `execute with unsupported value type`() {
        val thrown = assertThrows(FunctionExecutionException::class.java) {
            toBytesFunctionExecutor.execute("toBytes", this, emptyArray())
        }

        assertTrue(thrown.message!!.contains("Failed to convert to bytes the value: $this"))
        assertTrue(thrown.message!!.contains("It must be either String or Serializable object"))
    }

    @Test
    fun `get function name`() {
        assertEquals("toBytes", toBytesFunctionExecutor.getFunctionName())
    }
}