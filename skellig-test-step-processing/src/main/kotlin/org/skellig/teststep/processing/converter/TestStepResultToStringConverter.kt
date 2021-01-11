package org.skellig.teststep.processing.converter

import java.nio.charset.StandardCharsets

class TestStepResultToStringConverter : TestStepResultConverter {

     override fun convert(convertFunction: String, result: Any?): Any? {
        return if (result is ByteArray) {
            String(result, StandardCharsets.UTF_8)
        } else {
            result.toString()
        }
    }

    override fun getConvertFunctionName(): String {
        return "string"
    }
}