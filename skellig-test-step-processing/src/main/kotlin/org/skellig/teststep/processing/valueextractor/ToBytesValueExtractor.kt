package org.skellig.teststep.processing.valueextractor

import org.apache.commons.lang3.SerializationUtils
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import java.io.Serializable
import java.nio.charset.StandardCharsets

class ToBytesValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return when (value) {
            is String -> value.toByteArray(StandardCharsets.UTF_8)
            is Serializable -> SerializationUtils.serialize(value as Serializable?)
            else -> {
                throw ValueExtractionException(
                    """
                    Failed to convert to bytes the value: $value
                    It must be either String or Serializable object
                    """.trimIndent()
                )
            }
        }
    }

    override fun getExtractFunctionName(): String = "toBytes"
}