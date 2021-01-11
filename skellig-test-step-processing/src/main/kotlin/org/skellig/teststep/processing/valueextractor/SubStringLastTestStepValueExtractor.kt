package org.skellig.teststep.processing.valueextractor

class SubStringLastTestStepValueExtractor : SubStringTestStepValueExtractor() {

    override fun subStringAfter(value: String, after: String): String {
        return value.substringAfterLast(after)
    }

    override fun getExtractFunctionName(): String {
        return "subStringLast"
    }
}