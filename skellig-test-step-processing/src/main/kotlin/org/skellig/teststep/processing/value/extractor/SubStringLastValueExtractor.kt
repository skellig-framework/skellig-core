package org.skellig.teststep.processing.value.extractor

class SubStringLastValueExtractor : SubStringValueExtractor() {

    override fun subStringAfter(value: String, after: String): String {
        return value.substringAfterLast(after)
    }

    override fun getExtractFunctionName(): String {
        return "subStringLast"
    }
}