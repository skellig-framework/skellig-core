package org.skellig.teststep.processing.valueextractor

import io.restassured.path.json.JsonPath
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.exception.ValueExtractionException
import org.skellig.teststep.processing.experiment.ValueExtractor
import java.util.regex.Pattern

class JsonPathTestStepValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        if (args.isNotEmpty()) {
            return value?.let {
                try {
                    val json = JsonPath.from(it as String)
                    json.getString(args[0]?.toString())
                } catch (ex: Exception) {
                    if (args.size == 1 || (args.size == 2 && args[1]?.toString()?.trim() != "true")) {
                        throw ValueExtractionException("Failed to extract jsonPath '${args[0]}' from value '$value'. Reason ${ex.message}")
                    } else null
                }
            }
        } else {
            throw TestDataConversionException("Function `jsonPath` can only accept 1 or 2 arguments. Found ${args.size}")
        }
    }

    override fun getExtractFunctionName(): String {
        return "jsonPath"
    }

}