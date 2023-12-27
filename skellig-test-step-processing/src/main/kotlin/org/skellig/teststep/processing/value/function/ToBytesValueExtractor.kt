package org.skellig.teststep.processing.value.function

import org.apache.commons.lang3.SerializationUtils
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.io.Serializable
import java.nio.charset.StandardCharsets

class ToBytesValueExtractor : FunctionValueExecutor {

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