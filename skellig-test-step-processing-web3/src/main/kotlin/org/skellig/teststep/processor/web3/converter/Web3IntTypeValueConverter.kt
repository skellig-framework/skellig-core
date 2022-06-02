package org.skellig.teststep.processor.web3.converter

import org.skellig.teststep.processing.converter.TestStepValueConverter
import java.util.regex.Matcher
import java.util.regex.Pattern

class Web3IntTypeValueConverter : TestStepValueConverter {

    companion object {
        private val REGEX = Pattern.compile("^(uint|int)(\\d+)\\((\\d+)\\)$")
    }

    override fun convert(value: Any?): Any? =
        when (value) {
            is String -> {
                val matcher = REGEX.matcher(value.toString())
                if (matcher.find()) {
                    convert(matcher, "org.web3j.abi.datatypes.generated.")
                } else {
                    value
                }
            }
            else -> value
        }

    private fun convert(matcher: Matcher, className: String): Any? {
        val typePrefix = matcher.group(1).replaceFirstChar { it.uppercaseChar() }
        val size = matcher.group(2)
        val value = matcher.group(3)
        val type = Class.forName(className + typePrefix + size).kotlin

        return type.constructors.first().call(value.toBigInteger())
    }
}