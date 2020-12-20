package org.skellig.teststep.processing.valueextractor

import java.util.regex.Matcher
import java.util.regex.Pattern

class DefaultValueExtractor
private constructor(private val valueExtractors: Collection<TestStepValueExtractor>) : TestStepValueExtractor {

    companion object {
        private val EXTRACTION_PARAMETER_PATTERN = Pattern.compile("([\\w_-]+)\\((.+)\\)|\\((.+)\\)")
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return extractionParameter?.let {
            val matcher = EXTRACTION_PARAMETER_PATTERN.matcher(it)
            return if (matcher.find()) {
                val functionName = matcher.group(1)
                extract(functionName ?: "", value, getExtractionParameter(matcher))
            } else {
                extract("", value, extractionParameter)
            }
        } ?: value
    }

    private fun extract(extractFunctionName: String, value: Any?, parameter: String): Any? {
        return valueExtractors
                .filter { it.getExtractFunctionName() == extractFunctionName }
                .map { it.extract(value, parameter) }
                .first()
                ?: value
    }

    override fun getExtractFunctionName(): String? {
        return null
    }

    private fun getExtractionParameter(matcher: Matcher): String {
        return if (matcher.group(3) != null) matcher.group(3) else matcher.group(2)
    }

    class Builder {
        private var valueExtractors =
                mutableListOf(
                        JsonPathTestStepValueExtractor(),
                        XPathTestStepValueExtractor(),
                        ObjectTestStepValueExtractor(),
                        RegexTestStepValueExtractor())


        fun withValueExtractor(valueExtractor: TestStepValueExtractor) = apply {
            valueExtractors.add(valueExtractor)
        }

        fun build(): TestStepValueExtractor {
            return DefaultValueExtractor(valueExtractors)
        }
    }
}