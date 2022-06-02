package org.skellig.teststep.processor.web3.converter

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.web3j.abi.datatypes.Address
import org.web3j.utils.Numeric
import java.util.regex.Pattern

class Web3QuantityDecoder : TestStepValueConverter {

    companion object {
        private val REGEX = Pattern.compile("^(encodeQuantity|decodeQuantity)\\((\\w+)\\)$")
    }

    override fun convert(value: Any?): Any? =
        when (value) {
            is String -> {
                val matcher = REGEX.matcher(value.toString())
                if (matcher.find()) {
                    val innerValue = matcher.group(2)
                    when (matcher.group(1)) {
                        "encodeQuantity" -> Numeric.encodeQuantity(innerValue.toBigInteger())
                        "decodeQuantity" -> Numeric.decodeQuantity(innerValue)
                        else -> innerValue
                    }
                } else {
                    value
                }
            }
            else -> value
        }

}