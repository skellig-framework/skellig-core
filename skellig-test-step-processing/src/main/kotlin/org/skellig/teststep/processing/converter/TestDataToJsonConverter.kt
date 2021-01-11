package org.skellig.teststep.processing.converter

import com.fasterxml.jackson.databind.ObjectMapper

class TestDataToJsonConverter : TestDataConverter {

    companion object {
        private const val JSON_KEYWORD = "json"
    }

    private val jsonSerializer = ObjectMapper()

    override fun convert(testData: Any?): Any? {
        if (testData is Map<*, *>) {
            val valueAsMap = testData as Map<String, Any?>
            if (valueAsMap.containsKey(JSON_KEYWORD)) {
                val jsonContent = valueAsMap[JSON_KEYWORD]
                return jsonSerializer.writeValueAsString(jsonContent)
            }
        }
        return testData
    }
}