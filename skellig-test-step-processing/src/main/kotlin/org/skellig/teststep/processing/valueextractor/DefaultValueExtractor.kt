package org.skellig.teststep.processing.valueextractor

import java.util.regex.Matcher
import java.util.regex.Pattern

class DefaultValueExtractor
private constructor(private val valueExtractors: Collection<TestStepValueExtractor>) : TestStepValueExtractor {

    companion object {
        private val EXTRACTION_PARAMETER_PATTERN = Pattern.compile("([\\w_-]+)\\((.*)\\)|\\((.+)\\)")
    }

    override fun extract(value: Any?, extractionParameter: String?): Any? {
        return extractionParameter?.let {
            var newValue: Any? = value
            splitExtractionParameter(extractionParameter)
                    .forEach { p ->
                        val matcher = EXTRACTION_PARAMETER_PATTERN.matcher(p)
                        newValue = if (matcher.find()) {
                            val functionName = matcher.group(1)
                            extract(functionName ?: "", newValue, getExtractionParameter(matcher))
                        } else {
                            extract("", newValue, p)
                        }
                    }
            return newValue
        } ?: value
    }

    private fun splitExtractionParameter(extractionParameter: String): List<String> {
        val functions = mutableListOf<String>()
        val accumulator = StringBuilder()
        var bracketsOpened = false
        var quoteOpened = false
        extractionParameter.chars()
                .forEach {
                    when (val character = it.toChar()) {
                        '.' -> {
                            if (!bracketsOpened && !quoteOpened) {
                                functions.add(accumulator.toString())
                                accumulator.setLength(0)
                            } else accumulator.append(character)
                        }
                        '\'', '\"' -> {
                            if (accumulator.isEmpty() || accumulator[accumulator.length - 1] != '\\') {
                                quoteOpened = !quoteOpened
                            } else if (accumulator[accumulator.length - 1] == '\\') {
                                accumulator.deleteCharAt(accumulator.length - 1)
                            }
                            accumulator.append(character)
                        }
                        '(' -> {
                            bracketsOpened = true
                            accumulator.append(character)
                        }
                        ')' -> {
                            bracketsOpened = false
                            accumulator.append(character)
                        }
                        else -> accumulator.append(character)
                    }
                }
        if (accumulator.isNotEmpty()) functions.add(accumulator.toString())
        return functions
    }

    private fun extract(extractFunctionName: String, value: Any?, parameter: String): Any? {
        return valueExtractors
                .filter { it.getExtractFunctionName() == extractFunctionName }
                .map { it.extract(value, parameter) }
                .first()
    }

    override fun getExtractFunctionName(): String? {
        return null
    }

    private fun getExtractionParameter(matcher: Matcher): String {
        return if (matcher.group(3) != null) matcher.group(3) else matcher.group(2)
    }

    class Builder {
        private val valueExtractors =
                mutableListOf(
                        JsonPathTestStepValueExtractor(),
                        XPathTestStepValueExtractor(),
                        ObjectTestStepValueExtractor(),
                        FromIndexTestStepValueExtractor(),
                        RegexTestStepValueExtractor(),
                        ToStringTestStepValueExtractor(),
                        SubStringTestStepValueExtractor(),
                        SubStringLastTestStepValueExtractor())


        fun withValueExtractor(valueExtractor: TestStepValueExtractor) = apply {
            valueExtractors.add(valueExtractor)
        }

        fun build(): TestStepValueExtractor {
            return DefaultValueExtractor(valueExtractors)
        }
    }
}