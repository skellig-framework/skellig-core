package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.exception.ValueExtractionException

interface TestStepValueExtractor {

    @Throws(ValueExtractionException::class)
    fun extract(value: Any?, extractionParameter: String?): Any?

    fun getExtractFunctionName(): String
}