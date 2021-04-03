package org.skellig.teststep.processing.converter

import java.util.regex.Matcher
import java.util.regex.Pattern

class NumberValueConverter : TestStepValueConverter {

    companion object {
        private val INTEGER_REGEX = Pattern.compile("^(int|long)\\((\\d+)\\)$")
        private val FLOAT_REGEX = Pattern.compile("^(float|double)\\((\\d+\\.\\d+)\\)$")
    }

    override fun convert(value: Any?): Any? =
            when (value) {
                is String -> {
                    val intMatcher = INTEGER_REGEX.matcher(value.toString())
                    if (intMatcher.find()) {
                        toIntOrLong(intMatcher)
                    } else {
                        val floatMatcher = FLOAT_REGEX.matcher(value.toString())
                        if (floatMatcher.find())
                            toFloatOrDouble(floatMatcher)
                        else value
                    }
                }
                else -> value
            }

    private fun toFloatOrDouble(floatMatcher: Matcher): Number {
        val type = floatMatcher.group(1)
        val floatValue = floatMatcher.group(2)
        return if (type == "double") {
            floatValue.toDouble()
        } else {
            floatValue.toFloat()
        }
    }

    private fun toIntOrLong(intMatcher: Matcher): Number {
        val type = intMatcher.group(1)
        val intValue = intMatcher.group(2)
        return if (type == "int") {
            intValue.toInt()
        } else {
            intValue.toLong()
        }
    }
}