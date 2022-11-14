package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.experiment.FunctionValueProcessor
import java.util.regex.Pattern

internal class ListOfValueConverter : TestStepValueConverter, FunctionValueProcessor {

    companion object {
        private val LIST_REGEX = Pattern.compile("^listOf\\(([\\w\\s-,_.]*)\\)$")
        private val SEPARATOR_REGEX = Pattern.compile(",")
    }

    override fun execute(name: String, args: Array<Any?>): Any =args.toList()

    override fun getFunctionName(): String = "listOf"

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