package org.skellig.teststep.processing.valueextractor

import io.restassured.path.json.JsonPath
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import java.util.regex.Pattern

class JsonPathTestStepValueExtractor : TestStepValueExtractor, ValueExtractor {

    companion object {
        private val PARAM_SPLIT_PATTERN = Pattern.compile(",")
    }

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        if (args.isNotEmpty()) {
            return value?.let {
                try {
                    val json = JsonPath.from(it as String)
                    json.getString(args[0]?.toString())
                } catch (ex: Exception) {
                    if (args.size == 1 || (args.size == 2 && args[1]?.toString()?.trim() != "true")) {
                        throw ValueExtractionException(
                            "Failed to extract jsonPath '$args' from value '$value'. " +
                                    "Reason ${ex.message}"
                        )
                    } else null
                }
            } ?: throw ValueExtractionException("Failed to extract jsonPath from null value")
        } else {
            throw TestDataConversionException("Function `jsonPath` can only accept 1 or 2 arguments. Found ${args.size}")
        }
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return value?.let {
            val params = PARAM_SPLIT_PATTERN.split(extractionParameter ?: "")
            try {
                val json = JsonPath.from(it as String)
                json.getString(params[0])
            } catch (ex: Exception) {
                if (params.size == 1 || (params.size == 2 && params[1].trim() != "true")) {
                    throw ValueExtractionException(
                        "Failed to extract jsonPath '$extractionParameter' from value '$value'. " +
                                "Reason ${ex.message}"
                    )
                } else null
            }
        }
    }

    override fun getExtractFunctionName(): String {
        return "jsonPath"
    }

}