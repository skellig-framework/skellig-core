package org.skellig.teststep.processing.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule


class TestDataToJsonConverter : TestStepValueConverter {

    companion object {
        private const val JSON_KEYWORD = "json"
    }

    private val jsonSerializer = ObjectMapper()

    init {
        jsonSerializer.registerModule(JavaTimeModule())
        jsonSerializer.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    override fun convert(value: Any?): Any? {
        if (value is Map<*, *>) {
            val valueAsMap = value as Map<String, Any?>
            if (valueAsMap.containsKey(JSON_KEYWORD)) {
                val jsonContent = valueAsMap[JSON_KEYWORD]
                return jsonSerializer.writeValueAsString(jsonContent)
            }
        }
        return value
    }
}