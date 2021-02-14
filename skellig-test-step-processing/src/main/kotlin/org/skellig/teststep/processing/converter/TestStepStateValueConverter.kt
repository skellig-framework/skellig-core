package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor
import java.util.regex.Matcher
import java.util.regex.Pattern

class TestStepStateValueConverter(val testScenarioState: TestScenarioState,
                                  val valueExtractor: TestStepValueExtractor?) : TestStepValueConverter {

    companion object {
        private val GET_PATTERN = Pattern.compile("get\\(([\\w_$.-]+)\\)(\\.(.+))?")
    }

    override fun convert(value: String?): Any? {
        val matcher = GET_PATTERN.matcher(value)
        var result: Any? = value
        while (matcher.find() && result is String) {
            if (hasIdOnly(matcher)) {
                result = extractById(result, matcher)
            } else if (hasExtractFunction(matcher)) {
                result = extractUsingExtractorFunction(result, matcher)
            }
        }
        return result
    }

    private fun extractById(value: Any, matcher: Matcher): Any {
        val key = matcher.group(1)
        val valueFromState = testScenarioState.get(key)?: throwException(key)
        val originalValue = matcher.group(0)

        return (if (originalValue == value) valueFromState else replace(value, originalValue, valueFromState))
    }

    private fun extractUsingExtractorFunction(value: Any, matcher: Matcher): Any {
        val key = matcher.group(1)
        val valueFromState: Any = testScenarioState.get(key)?: throwException(key)
        val originalValue = matcher.group(0)
        val extractionParameter = matcher.group(3)
        val extractedValue = valueExtractor?.extract(valueFromState, extractionParameter)

        return (if (originalValue == value) extractedValue else replace(value, originalValue, extractedValue!!))!!
    }

    private fun replace(value: Any, toReplace: String, replaceWith: Any): String {
        return value.toString().replace(toReplace, replaceWith.toString())
    }

    private fun hasExtractFunction(matcher: Matcher): Boolean {
        return !hasIdOnly(matcher)
    }

    private fun hasIdOnly(matcher: Matcher): Boolean {
        return matcher.group(2) == null
    }

    private fun throwException(key: String): TestDataConversionException {
        throw TestDataConversionException("No data found in Test Scenario State with key $key")
    }
}