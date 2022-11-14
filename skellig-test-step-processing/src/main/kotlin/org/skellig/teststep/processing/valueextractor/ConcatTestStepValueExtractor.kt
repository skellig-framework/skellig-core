package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.experiment.ValueExtractor

class ConcatTestStepValueExtractor : TestStepValueExtractor, ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return value?.let {
            if (args.size == 1) {
                value.toString() + args[0]
            } else value
        }
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return value?.let {
            value.toString() + extractionParameter
        }
    }

    override fun getExtractFunctionName(): String = "concat"
}