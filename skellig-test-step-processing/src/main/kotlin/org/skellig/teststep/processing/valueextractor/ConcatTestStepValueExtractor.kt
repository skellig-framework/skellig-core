package org.skellig.teststep.processing.valueextractor

class ConcatTestStepValueExtractor : TestStepValueExtractor {

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return value?.let {
            value.toString() + extractionParameter
        }
    }

    override fun getExtractFunctionName(): String = "concat"
}