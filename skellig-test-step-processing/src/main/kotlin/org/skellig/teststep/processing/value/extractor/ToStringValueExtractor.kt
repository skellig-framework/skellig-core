package org.skellig.teststep.processing.value.extractor

import java.nio.charset.Charset

class ToStringValueExtractor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return value?.let {
            when (it) {
                is ByteArray -> String(it, Charset.forName(if(args.isEmpty()) "utf8" else args[0]?.toString()))
                else -> it.toString()
            }
        } ?: "null"
    }

    override fun getExtractFunctionName(): String {
        return "toString"
    }
}