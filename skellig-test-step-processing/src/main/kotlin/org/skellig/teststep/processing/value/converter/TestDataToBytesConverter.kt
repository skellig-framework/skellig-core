package org.skellig.teststep.processing.value.converter

import org.apache.commons.lang3.SerializationUtils
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.io.Serializable
import java.nio.charset.StandardCharsets

class TestDataToBytesConverter : TestDataConverter {

    companion object {
        private const val NAME = "toBytes"
        private const val DATA = "data"
    }

    override fun convert(data: Any?): Any? {
        return (data as? Map<*, *>)?.let {
            if (data.containsKey(NAME)) {
                if (data.containsKey(DATA)) {
                    return convertData((data[NAME] as Map<*, *>)[DATA])
                } else throw TestDataConversionException("Failed to find mandatory property 'data' for the data converter '${NAME}' in: ${data[NAME]}")

            } else convertData(data)
        }
    }

    private fun convertData(data: Any?): Any {
        return when (data) {
            is String -> data.toByteArray(StandardCharsets.UTF_8)
            is Serializable -> SerializationUtils.serialize(data as Serializable?)
            else -> {
                throw TestDataConversionException(
                    """
                    Failed to convert to bytes the value: $data
                    It must be either String or Serializable object
                    """.trimIndent()
                )
            }
        }
    }

    override fun getName(): String = NAME

    override fun isApplicable(data: Any?): Boolean {
        return data is Map<*, *> && data.size == 1 &&
                data.containsKey(NAME) && (data[NAME] as? Map<*, *>)?.containsKey(DATA) == true
    }

}