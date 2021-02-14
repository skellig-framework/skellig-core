package org.skellig.teststep.processing.valueextractor

import io.restassured.path.json.JsonPath
import org.skellig.teststep.processing.exception.ValueExtractionException
import java.util.regex.Pattern

class JsonPathTestStepValueExtractor : TestStepValueExtractor {

    companion object {
        private val PARAM_SPLIT_PATTERN = Pattern.compile(",")
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return value?.let {
            val params = PARAM_SPLIT_PATTERN.split(extractionParameter ?: "")
            try {
                val json = JsonPath.from(it as String)
                json.getString(params[0])
            } catch (ex: Exception) {
                if (params.size == 1 || (params.size == 2 && params[1].trim() != "true")) {
                    throw ValueExtractionException("Failed to extract jsonPath '$extractionParameter' from value '$value'. " +
                            "Reason ${ex.message}")
                } else null
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return "jsonPath"
    }

}