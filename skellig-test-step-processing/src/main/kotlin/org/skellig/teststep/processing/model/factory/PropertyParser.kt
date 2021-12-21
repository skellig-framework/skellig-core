package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor

internal class PropertyParser(
    private val propertyExtractorFunction: ((String) -> String?)?,
    private val valueExtractor: TestStepValueExtractor
) {

    companion object {
        private const val NULL = "null"
    }

    // identify all parameters or properties and process them
    // ${key_${key_${k3}}}
    // ${key_   ${key_   ${k3}  }  }
    //    0        1       2
    // ${key_   ${key_     v3  }  }
    // ${key_   v2                }
    // v1

    // ${key_1 : _${key_2 : _${k3 : v3}_ }_}
    // ${key_1 : _    ${key_2 : _    ${k3 : v3}   _}_   }
    //     0              1              2         3    4
    // ${key_1 : _    ${key_2 : _       v3      _}_   }   - 0
    // ${key_1 : _               v2_                  }   - 1

    // ${key_1       : _${key_2        : _${k3}_    }   _}
    // ${key_1                                                  - 1
    //                   _                                      - 2
    //                   _                _v3_                  - 3: read till counter is 2 (3 - 1) => 2
    //                   _                _v3_      ""          - 1
    //                   _                _v3_      ""   _      - 0 - finish

    // ${key_1       : _${key_2    }   _}
    // ${key_1                                 - 1
    //                                         - 2: not found => check if next char is ':'


    //     1              2*                 3                 4*
    //  ${key_1       : _${key_2        : _${k3}   _}     ${k4}   _}
    //  ${key_1                                                         - 1
    //                _v2                                               - 2: read till counter is 1 (2 - 1) => 1
    //                _v2                ""                             - skip, counter is 3, then 2
    //                _v2                ""          ""                 - skip, counter is 1
    //                _v2                ""          ""     v4          - 2: read till counter is 1 (2 - 1) => 1
    //                _v2                ""          ""     v4      _   - 0: finish

    //  _${key_${1}_}
    //  _  ${key_       ${1}      _}
    //  _  ${key_                     - 1: no default value active => chunk = key_ / keyFound = false
    //  _   key_       ${1}           - 2: no default value active => keyChunks += key_ / chunk = 1 / keyFound = false
    //  _   key_       a              - 1: keyChunks += a / keyFound = false
    //  _   key_       a         _}   - 0: chunk += _ => if(!keyFound) find(keyChunks + chunk)


    // ${key_1
    //               : _${key_2
    //               : _v2
    //                 _v2              ""
    //                 _v2              ""         ""

    // ${key_1
    //               : _${key_2
    //                 _                : _${k3         : v3
    //                 _                  _               v3    }_    }_   }

    // ${key_1
    //               : _${key_2
    //                  _              : _${k3
    //                  _               _value3
    //                  _               _value3          : v3    }_    }_   }
    //                  _               _value3           ""     }_    }_   }
    //                  _               _value3           ""      _    }_   }
    //                  _               _value3           ""      _     _

    fun parse(value: Any?, parameters: Map<String, Any?>): Any? =
        when (value) {
            is String -> {
                val result = InnerPropertyParser(this, propertyExtractorFunction, valueExtractor).innerConvert(value, false, 0, parameters)
                if (NULL == result) null else result
            }
            else -> value
        }

    class InnerPropertyParser(
        private val parser: PropertyParser,
        private val propertyExtractorFunction: ((String) -> String?)?,
        private val valueExtractor: TestStepValueExtractor
    ) {

        private var i = 0

        fun innerConvert(value: String, isNewKeyGroup: Boolean, group: Int, parameters: Map<String, Any?>): Any? {
            var chunk = ""
            var chunks: Any? = null
            var isInsideQuotes = false
            var isValueFound = false
            var isKeyReading = isNewKeyGroup
            var groupCounter = group
            var finalGroupNumber = 0
            while (i < value.length) {
                when (value[i]) {
                    '"', '\'' -> {
                        if (i == 0 || value[i - 1] != '\\') {
                            isInsideQuotes = !isInsideQuotes
                        }
                        // we still need to keep ' or " because they might be part of extraction path
                        // which will be processed in method 'convertValue'.
                        // Even if not in extraction path, these quotes will be dropped by 'convertValue'.
                        chunk += value[i]
                    }
                    '$' -> {
                        if (!isInsideQuotes && value[i + 1] == '{') {
//                        groupCounter++
                            if (chunk.isNotEmpty()) {
                                /*if(group == 0) {
                                    chunks += chunk
                                    chunk = ""
                                }*/
                            }
                            // ${${1}_abc}
                            // ${${1}_abc : 0}
                            finalGroupNumber = 0
                            isValueFound = false
//                        isKeyReading = true
                            i += 2
                            val result = innerConvert(value, true, 1, parameters)
                            if (chunk.isEmpty() && chunks == null) chunks = result
                            else if (chunks != null) {
                                chunks = chunks.toString() + chunk + result
                                chunk = ""
                            }
                            else chunk += result
                        } else {
                            chunk += value[i]
                        }
                    }
                    ' ' -> if (isInsideQuotes ||
                        (value[i + 1] != ':' && value[i - 1] != ':' &&
                                value[i + 1] != '}' /*&& value[i - 1] != '}'*/ &&
                                /*value[i + 1] != '{' &&*/ value[i - 1] != '{')
                    ) chunk += value[i]
                    ':' -> {
                        if (!isInsideQuotes) {
                            if (chunks != null && chunk.isNotEmpty()) chunk = chunks.toString() + chunk
                            convertValue(chunk, parameters)?.let {
                                isValueFound = true
                                chunks = if (chunks == null) it else chunks.toString() + it
                                finalGroupNumber = groupCounter - 1
                            } ?: run { isValueFound = false }
                            chunk = ""
                            isKeyReading = false
                        } else {
                            chunk += value[i]
                        }
                    }
                    '}' -> {
                        if (!isInsideQuotes) {
                            groupCounter--
                            if (isKeyReading) {
                                if (chunks != null && chunk.isNotEmpty()) chunk = chunks.toString() + chunk
                                convertValue(chunk, parameters)?.let {
                                    chunks = if (chunks == null) it else chunks.toString() + it
                                } ?: run {
                                    throw TestValueConversionException("No property or parameter found for key $chunk")
                                }
                            } else if (!isValueFound) {
                                chunks = if (chunks == null) chunk else if (chunk.isNotEmpty()) chunks.toString() + chunk else chunks
//                            isKeyReading = false
                            }

//                            chunk += value[i]
                            chunk = ""
                            if (groupCounter == 0) break
                        } else {
                            chunk += value[i]
                        }
                    }
                    else -> {
                        chunk += value[i]
                    }
                }
                i++
            }

            return if (chunk.isNotEmpty()) (chunks?.toString() ?: "") + chunk else chunks
        }

        private fun convertValue(value: String, parameters: Map<String, Any?>): Any? {
            var valueToConvert = ""
            var extractionPath = ""
            var isExtractionPath = false
            var isInsideQuotes = false
            var i = 0
            while (i < value.length) {
                if (isExtractionPath) {
                    extractionPath += value[i]
                } else {
                    when (value[i]) {
                        '"', '\'' -> {
                            if (i == 0 || value[i - 1] != '\\') {
                                isInsideQuotes = !isInsideQuotes
                            } else valueToConvert += value[i]
                        }
                        '.' -> {
                            if (!isInsideQuotes) isExtractionPath = true
                            else valueToConvert += value[i]
                        }
                        else -> valueToConvert += value[i]
                    }
                }
                i++
            }

//            return convertWithExtractions(valueToConvert, extractionPath, parameters)
            return getPropertyValue(value, parameters)
        }

        private fun convertWithExtractions(valueToConvert: String, extractionPath: String?, parameters: Map<String, Any?>): Any? {
            val result = getPropertyValue(valueToConvert, parameters)
            return extractionPath?.let { valueExtractor.extract(result, extractionPath) } ?: result
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

    /*private fun innerConvert(value: String, index : Int, parameters: Map<String, Any?>): String {
        var chunk = ""
        var chunks2 = ""
        var keyChunks = ""
        var currentChunkPosition = -1
        var chunks: MutableList<Any?>? = null
        var isInsideQuotes = false
        var isValueFound = false
        var isKeyReading = false
        var groupCounter = 0
        var finalGroupNumber = 0
        var i = index
        while (i < value.length) {
            when (value[i]) {
                '"', '\'' -> {
                    if (i == 0 || value[i - 1] != '\\') {
                        isInsideQuotes = !isInsideQuotes
                    }
                    // we still need to keep ' or " because they might be part of extraction path
                    // which will be processed in method 'convertValue'.
                    // Even if not in extraction path, these quotes will be dropped by 'convertValue'.
                    chunk += value[i]
                }
                '$' -> {
                    if (!isInsideQuotes && value[i + 1] == '{') {
                        groupCounter++
                        if (isValueFound && groupCounter >= finalGroupNumber) {
                            continue
                        } else {
                            if (chunk.isNotEmpty()) {
                                chunks = addToChunks(chunk, chunks)
                                *//*if (!isValueFound && groupCounter - 1 > 0) {
                                    keyChunks += chunk
                                } else {
                                    chunks2 += chunk
                                }*//*
                                chunks2 += innerConvert(value, i, parameters)
                                chunk = ""
                                currentChunkPosition++
                            }
                            finalGroupNumber = 0
                            isValueFound = false
                            isKeyReading = true
                            i++
                        }
                    } else {
                        chunk += value[i]
                    }
                }
                ' ' -> if (value[i + 1] != ':' && value[i + 1] != '}' && value[i - 1] != '{') chunk += value[i]
                ':' -> {
                    if (!isInsideQuotes && groupCounter > 0) {
                        if (isValueFound && groupCounter >= finalGroupNumber) {
                            continue
                        } else {
                            *//* if (isKeyReading && keyChunks.isNotEmpty()) {
                                 chunk = keyChunks + chunk
                                 keyChunks = ""
                             }*//*
                            convertValue(chunk, groupCounter > 0, parameters)?.let {
                                isValueFound = true
                                *//*  if (groupCounter > 0) {
                                      keyChunks += it
                                  } else {
                                      chunks2 += it
                                  }*//*
                                chunks2 += it
                                finalGroupNumber = groupCounter - 1
                            } ?: run { isValueFound = false }
                            chunk = ""
                            isKeyReading = false
                        }
                    }
                }
                '}' -> {
                    if (!isInsideQuotes && groupCounter > 0) {
                        if (isValueFound && groupCounter - 1 >= finalGroupNumber) {
                            groupCounter--
                            continue
                        } else {
                            if (isKeyReading) {
                                groupCounter--
                                *//*if (groupCounter == 0) {
                                    chunk = keyChunks + chunk
                                    keyChunks = ""
                                }*//*
                                convertValue(chunk, groupCounter > 0, parameters)?.let {
                                    //                                            isValueFound = true
                                    *//*if (groupCounter > 0) {
                                        keyChunks += it
                                    } else {
                                        chunks2 += it
                                    }*//*
                                    chunks2 += it
                                } ?: run {
                                    throw TestValueConversionException("No property or parameter found for key $chunk")
                                }
                            } else {
                                *//*if (groupCounter > 0) {
                                    keyChunks += chunk
                                } else {
                                    chunks2 += chunk
                                }*//*
                                chunks2 += chunk
                                //                                        isValueFound = true
                                isKeyReading = false
                            }

                            chunk += value[i]
                            chunks = addToChunks(chunk, chunks)
                            chunk = ""
                        }
                    } else {
                        chunk += value[i]
                    }
                }
                else -> {
                    chunk += value[i]
                }
            }
            i++
        }

        return chunks2 + chunk
    }*/
}