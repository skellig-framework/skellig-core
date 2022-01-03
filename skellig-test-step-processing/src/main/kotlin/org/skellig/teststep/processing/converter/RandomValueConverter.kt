package org.skellig.teststep.processing.converter

import org.apache.commons.lang3.StringUtils
import org.skellig.teststep.processing.exception.TestDataConversionException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import kotlin.random.Random


class RandomValueConverter : TestStepValueConverter {

    companion object {
        private val NOW_PATTERN = Pattern.compile("rand\\(([\\w.]*)\\s*,\\s*([\\w.]+)\\s*,?\\s*([\\w.]*)\\)")
    }

    private val random = Random(System.currentTimeMillis())

    override fun convert(value: Any?): Any? =
        when (value) {
            is String -> {
                val matcher = NOW_PATTERN.matcher(value.toString())
                if (matcher.find()) {
                    val min = if (matcher.group(1) == "") "0" else matcher.group(1)
                    val max = matcher.group(2)
                    when (matcher.group(3)) {
                        "int" -> random.nextInt(min.toInt(), max.toInt())
                        "long" -> random.nextLong(min.toLong(), max.toLong())
                        "double" -> random.nextDouble(min.toDouble(), max.toDouble())
                        "bigDecimal" -> randomBigDecimal(min.toBigDecimal(), max.toBigDecimal())
                        else -> random.nextLong(min.toLong(), max.toLong())
                    }

                } else value
            }
            else -> value
        }

    private fun randomBigDecimal(min : BigDecimal, max : BigDecimal): BigDecimal {
        val randomBigDecimal = min.add(BigDecimal(random.nextDouble()).multiply(max.subtract(min)))
        return randomBigDecimal.setScale(0, RoundingMode.HALF_UP)
    }
}