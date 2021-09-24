package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.SerializationUtils
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.io.Serializable
import java.nio.charset.StandardCharsets

class TestDataToBytesConverter(val testStepValueConverter: TestStepValueConverter) : TestStepValueConverter {

    companion object {
        private const val VALUE = "value"
        private const val TO_BYTES = "toBytes"
    }

    override fun convert(value: Any?): Any? {
        var newTestData = value
        if (value is Map<*, *> && value.containsKey(TO_BYTES)) {
            val toBytes = value[TO_BYTES] as Map<*, *>
            val valueToConvert = testStepValueConverter.convert(toBytes[VALUE] ?: toBytes)
            newTestData = when (valueToConvert) {
                is String -> {
                    valueToConvert.toByteArray(StandardCharsets.UTF_8)
                }
                is Serializable -> {
                    SerializationUtils.serialize(valueToConvert as Serializable?)
                }
                else -> {
                    throw TestDataConversionException(String.format("""
                    Failed to convert to bytes the value: %s
                    It must be either String or Serializable object
                    """.trimIndent(), newTestData))
                }
            }
        }
        return newTestData
    }
}