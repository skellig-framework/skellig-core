package org.skellig.teststep.processing.value.function

import org.apache.commons.lang3.SerializationUtils
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.io.Serializable
import java.nio.charset.StandardCharsets

/**
 * Executes a function 'toBytes' by converting the provided value to a byte array.
 * The conversion is performed based on the type of the value:
 * - If the value is a String, it is converted to a byte array using UTF-8 encoding.
 * - If the value is a Serializable object, it is serialized and converted to a byte array.
 * If the value is neither a String nor a Serializable object, a [FunctionExecutionException] is thrown.
 *
 * Supported args:
 * - toBytes() - called in a call chain of the 'value'
 */
class ToBytesFunctionExecutor : FunctionValueExecutor {

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? {
        return when (value) {
            is String -> value.toByteArray(StandardCharsets.UTF_8)
            is Serializable -> SerializationUtils.serialize(value as Serializable?)
            else -> {
                throw FunctionExecutionException(
                    """
                    Failed to convert to bytes the value: $value
                    It must be either String or Serializable object
                    """.trimIndent()
                )
            }
        }
    }

    override fun getFunctionName(): String = "toBytes"
}