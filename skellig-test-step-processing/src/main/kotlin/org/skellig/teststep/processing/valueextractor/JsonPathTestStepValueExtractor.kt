package org.skellig.teststep.processing.valueextractor

import io.restassured.path.json.JsonPath
import org.skellig.teststep.processing.exception.ValueExtractionException
import java.lang.String.format

class JsonPathTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value?.let {
            val json = JsonPath.from(it as String)
            json.getString(extractionParameter)
        } ?: throw ValueExtractionException(format("Cannot extract jsonPath '%s' from null value", extractionParameter))
    }

    override fun getExtractFunctionName(): String {
        return "jsonPath"
    }

}