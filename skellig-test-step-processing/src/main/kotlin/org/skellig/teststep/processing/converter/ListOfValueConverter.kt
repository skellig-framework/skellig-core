package org.skellig.teststep.processing.converter

import java.util.regex.Pattern

internal class ListOfValueConverter : TestStepValueConverter {

    companion object {
        private val LIST_REGEX = Pattern.compile("^listOf\\(([\\w\\s-,_.]*)\\)$")
        private val SEPARATOR_REGEX = Pattern.compile(",")
    }

    override fun convert(value: String?): Any? {
        return value?.let {
            val matcher = LIST_REGEX.matcher(value)
            if (matcher.find()) {
                val listValue = matcher.group(1)
                return SEPARATOR_REGEX.split(listValue).map { it.trim() }.filter { it.trim().isNotEmpty() }.toList()
            }
            return value
        } ?: value
    }
}