package org.skellig.teststep.processing.converter

import java.util.regex.Pattern

class NumberValueConverter : TestStepValueConverter {

    companion object {
        private val INTEGER_REGEX = Pattern.compile("^\\((int|long|short)\\) *(\\d+)$")
        private val FLOAT_REGEX = Pattern.compile("^\\((float|double)\\) *(\\d+\\.\\d+)$")
    }

    override fun convert(value: String?): Any? {
        val intMatcher = INTEGER_REGEX.matcher(value)
        if (intMatcher.find()) {
            val type = intMatcher.group(1)
            val intValue = intMatcher.group(2)
            return if (type == "int") {
                intValue.toInt()
            } else {
                intValue.toLong()
            }
        } else {
            val floatMatcher = FLOAT_REGEX.matcher(value)
            if (floatMatcher.find()) {
                val type = floatMatcher.group(1)
                val floatValue = floatMatcher.group(2)
                return if (type == "double") {
                    floatValue.toDouble()
                } else {
                    floatValue.toFloat()
                }
            }
        }
        return value
    }
}