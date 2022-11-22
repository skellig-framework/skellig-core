package org.skellig.teststep.processing.value.extractor

interface ValueExtractor {

    fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any?

    fun getExtractFunctionName(): String
}