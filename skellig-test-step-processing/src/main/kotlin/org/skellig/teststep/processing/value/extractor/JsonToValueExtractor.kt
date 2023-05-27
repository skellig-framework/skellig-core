package org.skellig.teststep.processing.value.extractor

import com.fasterxml.jackson.databind.ObjectMapper
import org.skellig.teststep.processing.value.extractor.exception.ValueExtractionException

class JsonToMapTestStepValueExtractor : JsonToTestStepValueExtractor() {

    override fun getToClassConversion(): Class<*> = Map::class.java

    override fun getDefaultValueForNull(): Any = emptyMap<Any, Any>()

    override fun getExtractFunctionName(): String = "jsonToMap"
}

class JsonToListTestStepValueExtractor : JsonToTestStepValueExtractor() {

    override fun getToClassConversion(): Class<*> = List::class.java

    override fun getDefaultValueForNull(): Any = emptyList<Any>()

    override fun getExtractFunctionName(): String = "jsonToList"

}

abstract class JsonToTestStepValueExtractor : ValueExtractor {

    private val objectMapper = ObjectMapper()

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        val valueAsString = value?.toString() ?: ""
        return if (value == null || valueAsString.isEmpty()) {
            getDefaultValueForNull()
        } else {
            try {
                objectMapper.readValue(value.toString(), getToClassConversion())
            } catch (ex: Exception) {
                throw ValueExtractionException(
                    "Failed to convert JSON to ${getToClassConversion().simpleName}: '$value'",
                    ex
                )
            }
        }
    }

    abstract fun getToClassConversion(): Class<*>

    abstract fun getDefaultValueForNull(): Any
}