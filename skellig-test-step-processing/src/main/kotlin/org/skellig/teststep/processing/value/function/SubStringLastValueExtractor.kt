package org.skellig.teststep.processing.value.function

class SubStringLastValueExtractor : SubStringValueExtractor() {

    override fun subStringAfter(value: String, after: String): String {
        return value.substringAfterLast(after)
    }

    override fun getFunctionName(): String {
        return "subStringLast"
    }
}