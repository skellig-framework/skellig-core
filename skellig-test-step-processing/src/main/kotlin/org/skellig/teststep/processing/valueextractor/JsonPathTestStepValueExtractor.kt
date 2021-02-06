package org.skellig.teststep.processing.valueextractor

import io.restassured.path.json.JsonPath
import org.skellig.teststep.processing.exception.ValueExtractionException

class JsonPathTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value?.let {
            try {
                val json = JsonPath.from(it as String)
                json.getString(extractionParameter)
            } catch (ex: Exception) {
                throw ValueExtractionException("Failed to extract jsonPath '$extractionParameter' from value $value. " +
                        "Reason ${ex.message}")
            }
        } ?: throw ValueExtractionException("Cannot extract jsonPath '$extractionParameter' from null value")
    }

    override fun getExtractFunctionName(): String {
        return "jsonPath"
    }

}