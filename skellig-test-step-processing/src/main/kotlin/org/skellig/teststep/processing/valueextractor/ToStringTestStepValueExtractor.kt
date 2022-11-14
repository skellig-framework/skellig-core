package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.experiment.ValueExtractor
import java.nio.charset.Charset

class ToStringTestStepValueExtractor : TestStepValueExtractor, ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any {
        return value?.let {
            when (it) {
                is ByteArray -> String(it, Charset.forName(if(args.isEmpty()) "utf8" else args[0]?.toString()))
                else -> it.toString()
            }
        } ?: "null"
    }

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