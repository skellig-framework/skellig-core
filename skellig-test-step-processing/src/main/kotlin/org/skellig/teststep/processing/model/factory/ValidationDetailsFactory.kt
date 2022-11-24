package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.ExpectedResult
import org.skellig.teststep.processing.model.MatchingType
import org.skellig.teststep.processing.model.ValidationDetails
import org.skellig.teststep.processing.value.chunk.RawValueChunkParser
import java.util.*
import java.util.regex.Pattern

class ValidationDetailsFactory(val keywordsProperties: Properties? = null) {

    companion object {
        private val GROUPED_PROPERTIES_PATTERN = Pattern.compile("\\[([\\w,\\s]+)\\]")
        private val INDEX_PROPERTY_PATTERN = Pattern.compile("\\[\\s*\\d+\\s*\\]")
        private val SPLIT_PATTERN = Pattern.compile(",")
    }

    private var validationKeywords: Set<String>
    private var validationTypeKeywords: Map<String?, MatchingType>
    private val rawValueChunkParser = RawValueChunkParser()

    init {
        validationKeywords = setOf(
            getKeywordName("test.step.keyword.validate", "validate"),
            getKeywordName("test.step.keyword.expected_result", "expected result"),
            getKeywordName("test.step.keyword.expected_response", "expected response"),
            getKeywordName("test.step.keyword.expected_message", "expected message"),
            getKeywordName("test.step.keyword.assert", "assert")
        )

        validationTypeKeywords = mapOf(
            Pair(getKeywordName("test.step.keyword.all_match", "all_match"), MatchingType.ALL_MATCH),
            Pair(getKeywordName("test.step.keyword.any_match", "any_match"), MatchingType.ANY_MATCH),
            Pair(getKeywordName("test.step.keyword.none_match", "none_match"), MatchingType.NONE_MATCH),
            Pair(getKeywordName("test.step.keyword.any_none_match", "any_none_match"), MatchingType.ANY_NONE_MATCH),
        )
    }

    fun create(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): ValidationDetails? {
        val rawValidationDetails = getValidationDetails(rawTestStep)
        val builder = ValidationDetails.Builder()

        return rawValidationDetails?.let {
            if (rawValidationDetails is Map<*, *>) {
                val fromTestId = rawValidationDetails[getFromTestKeyword()]

                if (fromTestId != null) {
                    builder.withTestStepId(fromTestId as String?)
                    val rawExpectedResult =
                        rawValidationDetails
                            .filter { item -> item.key != getFromTestKeyword() }
                            .map { item -> item.key to item.value }
                            .toMap()

                    builder.withExpectedResult(createExpectedResult("", rawExpectedResult, parameters))
                } else {
                    builder.withExpectedResult(createExpectedResult("", it, parameters))
                }
            } else {
                builder.withExpectedResult(createExpectedResult("", it, parameters))
            }
            builder.build()
        }
    }

    private fun getValidationDetails(rawTestStep: Map<String, Any?>): Any? {
        return validationKeywords
            .map { rawTestStep[it] }
            .firstOrNull { it != null }
    }

    private fun createExpectedResult(propertyName: String?, expectedResult: Any?, parameters: Map<String, Any?>): ExpectedResult {
        var newPropertyName = propertyName
        var newExpectedResult = expectedResult

        var validationType: MatchingType? = MatchingType.ALL_MATCH
        if (validationTypeKeywords.containsKey(propertyName)) {
            validationType = validationTypeKeywords[propertyName]
            newPropertyName = null
        }

        if (newExpectedResult is Map<*, *>) {
            val expectedResultAsMap = newExpectedResult as Map<String, Any?>
            // If expectedResult has only 1 key and it is a validation type, then extract the value
            // and assign it to the current propertyName
            if (expectedResultAsMap.size == 1) {
                val entry = expectedResultAsMap.entries.first()
                if (validationTypeKeywords.containsKey(entry.key)) {
                    validationType = validationTypeKeywords[entry.key]
                    newExpectedResult = entry.value
                }
            }
            if (newExpectedResult === expectedResultAsMap || newExpectedResult is Map<*, *>) {
                newExpectedResult = createExpectedResults(extendExpectedResultMapIfApplicable(newExpectedResult as Map<String, Any>), parameters)
            } else if (newExpectedResult is List<*>) {
                newExpectedResult = createExpectedResults(newExpectedResult as List<Any>, parameters)
            }
        } else if (newExpectedResult is List<*>) {
            newExpectedResult = createExpectedResults(newExpectedResult as List<Any>, parameters)
        } else if (newExpectedResult is String) {
            newExpectedResult = rawValueChunkParser.buildFrom(newExpectedResult, parameters)
            validationType = null
        } else {
            validationType = null
        }
        return ExpectedResult(
            if (newPropertyName != null) rawValueChunkParser.buildFrom(newPropertyName, parameters) else null,
            newExpectedResult, validationType
        )
    }

    private fun createExpectedResults(expectedResult: Map<String, Any?>, parameters: Map<String, Any?>): Any {
        return expectedResult
            .map { createExpectedResult(it.key, it.value, parameters) }
            .toList()
    }

    private fun createExpectedResults(expectedResult: List<Any>, parameters: Map<String, Any?>): Any {
        return expectedResult
            .map { createExpectedResult(null, it, parameters) }
            .toList()
    }

    /**
     * If expected result contains construction like: [n1,n2...n] as a key, then it must be split by comma
     * and for each element assign the value of the original key and put it back to the new Map
     */
    private fun extendExpectedResultMapIfApplicable(expectedResultAsMap: Map<String, Any?>): Map<String, Any?> {
        return if (hasSplitProperties(expectedResultAsMap)) {
            val extendedExpectedResultAsMap: MutableMap<String, Any?> = HashMap()
            expectedResultAsMap.forEach { (key: String, value: Any?) ->
                val matcher = GROUPED_PROPERTIES_PATTERN.matcher(key)
                if (matcher.find()) {
                    for (newPropertyName in SPLIT_PATTERN.split(matcher.group(1))) {
                        extendedExpectedResultAsMap[newPropertyName.trim { it <= ' ' }] = value
                    }
                } else {
                    extendedExpectedResultAsMap[key] = value
                }
            }
            extendedExpectedResultAsMap
        } else {
            expectedResultAsMap
        }
    }

    private fun hasSplitProperties(expectedResultAsMap: Map<String, Any?>): Boolean {
        return expectedResultAsMap.keys
            .any { it.startsWith("[") && !INDEX_PROPERTY_PATTERN.matcher(it).matches() }
    }

    private fun getFromTestKeyword(): String {
        return getKeywordName("test.step.keyword.from_test", "fromTest")
    }

    private fun getKeywordName(keywordName: String?, defaultValue: String): String {
        return if (keywordsProperties == null) defaultValue else keywordsProperties.getProperty(keywordName, defaultValue)
    }
}