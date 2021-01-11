package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.SerializationUtils
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.io.Serializable
import java.nio.charset.StandardCharsets

class TestDataToBytesConverter : TestDataConverter {

    companion object {
        private const val VALUE = "value"
        private const val TO_BYTES = "toBytes"
    }

    override fun convert(testData: Any?): Any? {
        var newTestData = testData
        if (testData is Map<*, *>) {
            val valueAsMap = testData as Map<String, Any>
            if (valueAsMap.containsKey(TO_BYTES)) {
                val toBytes = valueAsMap[TO_BYTES] as Map<String, Any?>
                val value = toBytes[VALUE]
                newTestData = when {
                    value is String -> {
                        value.toByteArray(StandardCharsets.UTF_8)
                    }
                    testData is Serializable -> {
                        SerializationUtils.serialize(testData as Serializable?)
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