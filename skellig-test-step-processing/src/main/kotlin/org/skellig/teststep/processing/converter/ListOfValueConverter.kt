package org.skellig.teststep.processing.converter

import java.util.regex.Pattern

internal class ListOfValueConverter : TestStepValueConverter {

    companion object {
        private val LIST_REGEX = Pattern.compile("^listOf\\(([\\w\\s-,_.]*)\\)$")
        private val SEPARATOR_REGEX = Pattern.compile(",")
    }

    override fun convert(value: Any?): Any? =
            when (value) {
                is String -> {
                    val matcher = LIST_REGEX.matcher(value.toString())
                    if (matcher.find()) {
                        val listValue = matcher.group(1)
                        SEPARATOR_REGEX.split(listValue).map { it.trim() }.filter { it.trim().isNotEmpty() }.toList()
                    } else value
                }
                else -> value
            }
}