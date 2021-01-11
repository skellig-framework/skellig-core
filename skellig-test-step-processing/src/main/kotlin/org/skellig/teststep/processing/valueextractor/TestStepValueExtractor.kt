package org.skellig.teststep.processing.valueextractor

interface TestStepValueExtractor {

    fun extract(value: Any?, extractionParameter: String?): Any?

    fun getExtractFunctionName(): String?
}