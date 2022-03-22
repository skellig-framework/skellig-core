package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

internal class PropertyParser(
    private val propertyExtractorFunction: ((String) -> Any?)?,
    private val valueExtractor: TestStepValueExtractor
) {

    companion object {
        private const val NULL = "null"
    }

    fun parse(value: Any?, parameters: Map<String, Any?>): Any? =
        when (value) {
            is String -> {
                val result =
                    if (value.isEmpty()) ""
                    else InnerPropertyParser(this, propertyExtractorFunction, valueExtractor).innerParse(value, false, 0, false, 0, parameters)

                if (NULL == result) null else result
            }
            else -> value
        }

    inner class InnerPropertyParser(
        private val parser: PropertyParser,
        private val propertyExtractorFunction: ((String) -> Any?)?,
        private val valueExtractor: TestStepValueExtractor
    ) {

        private var i = 0

        fun innerParse(
            value: String, isNewKeyGroup: Boolean, group: Int,
            isNewGroupWithExtractors: Boolean = false, newExtractorsGroupCounter: Int = 0,
            parameters: Map<String, Any?>
        ): Any? {
            // chunk is an accumulator, collecting the characters from value
            var chunk = ""
            var finalResult: Any? = null
            // true if inside ' or " meaning it will read all characters including special ones
            var isInsideQuotes = false
            var isValueFound = false
            // true if we get group like #[ ${...}.extractionPath ] which maybe has extractionPath, ending on ']'
            var containsExtractors = isNewGroupWithExtractors
            // counter of #[ ] groups which can have extractors
            var extractorsGroupCounter = newExtractorsGroupCounter
            // true if right now it reads the property key inside ${ }. False otherwise or if default value is reading after :
            var isKeyReading = isNewKeyGroup
            // counter of ${ } groups with references to properties/params/variables
            var groupCounter = group

            while (i < value.length) {
                if (value[i] == '"' || value[i] == '\'') {
                    if (i == 0 || value[i - 1] != '\\') {
                        isInsideQuotes = !isInsideQuotes
                    }
                    // we still need to keep ' or " because they might be part of extraction path
                    // which will be processed in method 'convertValue'.
                    // Even if not in extraction path, these quotes will be dropped by 'convertValue'.
                    chunk += value[i]
                } else if (!isInsideQuotes && value[i] == '#' && value[i + 1] == '[') {   // start processing group of #[ ] if valid
                    // verify that after #[ we have property reference, ex: ${a}
                    var j = i + 2
                    while (j < value.length - 1) {
                        if (value[j] == ' ') {
                            j++
                        } else {
                            if (value[j] == '$' && value[j + 1] == '{') {
                                containsExtractors = true
                                extractorsGroupCounter++
                            }
                            j = value.length
                        }
                    }
                    if (containsExtractors) {
                        i += 2
                        // read the group #[ ] in isolation and extract the value if any
                        val result = innerParse(value, false, 0, true, 1, parameters)

                        if (chunk.isEmpty() && finalResult == null) finalResult = result
                        else if (finalResult != null) {
                            finalResult = finalResult.toString() + chunk + result
                            chunk = ""
                        } else chunk += result
                    } else chunk += value[i]
                } else if (!isInsideQuotes && value[i] == ']' && --extractorsGroupCounter <= 0 && containsExtractors) {
                    // if the group was #[ ] and ended on ] then take the result and extract from it, then return from the method
                    if (chunk.isNotEmpty() && chunk[0] == '.') finalResult = chunk.let { valueExtractor.extract(finalResult, chunk) } ?: finalResult
                    chunk = ""
                    if (extractorsGroupCounter == 0) break
                } else if (!isInsideQuotes && value[i] == '$' && value[i + 1] == '{') {   // start processing group of ${ }
                    isValueFound = false
                    i += 2
                    val result = innerParse(value, true, 1, false, 0, parameters)

                    if (chunk.isEmpty() && finalResult == null) finalResult = result
                    else if (finalResult != null && !containsExtractors) {
                        finalResult = finalResult.toString() + chunk + result
                        chunk = ""
                    } else chunk += result
                }
                // ignore a space if it's near some special chars
                else if (value[i] == ' ') {
                    if (isInsideQuotes ||
                        // accept space if next or previous char is : only if it's not inside a property group, ex "a : b : c"
                        (((value[i + 1] != ':' && value[i - 1] != ':') || (groupCounter == 0 && (value[i + 1] == ':' || value[i - 1] == ':'))) &&
                                value[i + 1] != '}' && value[i - 1] != '{' &&
                                value[i + 1] != ']' && value[i - 1] != '[')
                    ) chunk += value[i]
                }
                // process default value if property value for a key not found, or if found then ignore what comes after : till the end of the group
                else if (!isInsideQuotes && value[i] == ':' && isKeyReading) {
                    val propertyValue = getPropertyValue(chunk, parameters)
                    if (propertyValue != null) {
                        isValueFound = true
                        // finalResult can be any type but if null, then just assign the property value because it can be not String
                        finalResult = if (finalResult == null) propertyValue else finalResult.toString() + propertyValue

                        // if property value found, then we can skip default value thus reading till the end of the group
                        readTillTheEndOfPropertyGroup(value)
                        chunk = ""
                        isKeyReading = false
                        continue
                    } else isValueFound = false
                    chunk = ""
                    isKeyReading = false
                } else if (!isInsideQuotes && value[i] == '}') {     // process the end of the property group
                    groupCounter--
                    if (isKeyReading) {
                        getPropertyValue(chunk, parameters)?.let {
                            finalResult = if (finalResult == null) it else finalResult.toString() + it
                        } ?: run { throw TestValueConversionException("No property or parameter found for key $chunk") }
                    } else if (!isValueFound) {
                        finalResult = if (finalResult == null) chunk else if (chunk.isNotEmpty()) finalResult.toString() + chunk else finalResult
                    }
                    chunk = ""
                    if (groupCounter == 0) break
                } else if (!isInsideQuotes && value[i] == '\\' && (i + 1 < value.length &&
                            (value[i + 1] == ':' || value[i + 1] == '}' || value[i + 1] == '{' ||
                                    value[i + 1] == ']' || value[i + 1] == '['))
                ) {
                    chunk += value[i + 1]
                    i++
                } else {
                    chunk += value[i]
                }
                i++
            }

            return if (chunk.isNotEmpty()) (finalResult?.toString() ?: "") + chunk else finalResult
        }

        private fun readTillTheEndOfPropertyGroup(value: String) {
            var keyGroup = 1
            var j = i + 1
            var isEnclosedInQuotes = false
            while (j < value.length && keyGroup > 0) {
                if (value[j] == '\'' || value[j] == '"') isEnclosedInQuotes = !isEnclosedInQuotes
                else if (!isEnclosedInQuotes && value[j] == '{') keyGroup++
                else if (!isEnclosedInQuotes && value[j] == '}') keyGroup--
                i++
                j++
            }
        }

        private fun getPropertyValue(propertyKey: String, parameters: Map<String, Any?>): Any? {
            var propertyValue: Any? = null
            if (propertyExtractorFunction != null) {
                propertyValue = propertyExtractorFunction.invoke(propertyKey)
            }
            if (propertyValue == null && parameters.containsKey(propertyKey)) {
                val value = parameters[propertyKey]
                if (!(value is String && value.isEmpty())) {
                    propertyValue = parser.parse(value, parameters)
                }
            }
            if (propertyValue == null) {
                propertyValue = System.getProperty(propertyKey)
            }
            if (propertyValue == null) {
                propertyValue = System.getenv(propertyKey)
            }
            return propertyValue
        }
    }
}