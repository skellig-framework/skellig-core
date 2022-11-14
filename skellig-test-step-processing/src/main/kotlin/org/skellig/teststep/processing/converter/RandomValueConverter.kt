package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.experiment.FunctionValueProcessor
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Pattern
import kotlin.random.Random


class RandomValueConverter : TestStepValueConverter, FunctionValueProcessor {

    companion object {
        private val NOW_PATTERN = Pattern.compile("rand\\(([\\w.]*)\\s*,\\s*([\\w.]+)\\s*,?\\s*([\\w.]*)\\)")
    }

    private val random = Random(System.currentTimeMillis())

    override fun execute(name: String, args: Array<Any?>): Any {
        if (args.size in 2..3) {
            val min = args[0]?.toString() ?: ""
            val max = args[1]?.toString() ?: ""
            return when (args[3]) {
                "int" -> random.nextInt(min.toInt(), max.toInt())
                "long" -> random.nextLong(min.toLong(), max.toLong())
                "double" -> random.nextDouble(min.toDouble(), max.toDouble())
                "bigDecimal" -> randomBigDecimal(min.toBigDecimal(), max.toBigDecimal())
                else -> random.nextLong(min.toLong(), max.toLong())
            }
        } else {
            throw TestDataConversionException("Function `rand` can only accept between 2 and 3 String arguments. Found ${args.size}")
        }
    }

    override fun getFunctionName(): String = "rand"

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

    private fun randomBigDecimal(min: BigDecimal, max: BigDecimal): BigDecimal {
        val randomBigDecimal = min.add(BigDecimal(random.nextDouble()).multiply(max.subtract(min)))
        return randomBigDecimal.setScale(0, RoundingMode.HALF_UP)
    }
}