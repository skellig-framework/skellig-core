package org.skellig.teststep.processing.value.extractor

class FindFromStateValueExtractor(val valueExtractor: ObjectValueExtractor?) : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return when (value) {
            is List<*> -> value.map { extractFrom(name, it, args) }.firstOrNull()
            else -> try {
                valueExtractor?.extractFrom("", value, args)
            } catch (ex: Exception) {
                null
            }
        }
    }

    override fun getExtractFunctionName(): String = "find"

}