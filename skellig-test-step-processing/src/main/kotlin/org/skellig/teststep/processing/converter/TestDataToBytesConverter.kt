package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.SerializationUtils
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.io.Serializable
import java.nio.charset.StandardCharsets

class TestDataToBytesConverter : TestStepValueConverter {

    companion object {
        private const val VALUE = "value"
        private const val TO_BYTES = "toBytes"
    }

    override fun convert(value: Any?): Any? {
        var newTestData = value
        if (value is Map<*, *>) {
            val valueAsMap = value as Map<String, Any>
            if (valueAsMap.containsKey(TO_BYTES)) {
                val toBytes = valueAsMap[TO_BYTES] as Map<String, Any?>
                val value = toBytes[VALUE]
                newTestData = when {
                    value is String -> {
                        value.toByteArray(StandardCharsets.UTF_8)
                    }
                    value is Serializable -> {
                        SerializationUtils.serialize(value as Serializable?)
                    }
                    else -> {
                        throw TestDataConversionException(String.format("""
            Failed to convert to bytes the test data: %s
            It must be either String or Serializable object
            """.trimIndent(), newTestData))
                    }
                }
            }
        }
        return newTestData
    }
}