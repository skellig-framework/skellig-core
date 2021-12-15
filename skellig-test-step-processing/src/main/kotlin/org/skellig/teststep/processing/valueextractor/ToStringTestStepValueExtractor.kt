package org.skellig.teststep.processing.valueextractor

import java.nio.charset.Charset

class ToStringTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any {
        return value?.let {
            when (it) {
                is ByteArray -> String(it,
                        Charset.forName(if(extractionParameter.isNullOrEmpty()) "utf8" else extractionParameter))
                else -> it.toString()
            }
        } ?: "null"
    }

    override fun getExtractFunctionName(): String {
        return "toString"
    }
}