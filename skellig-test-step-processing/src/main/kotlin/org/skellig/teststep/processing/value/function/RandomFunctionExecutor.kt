package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.exception.TestDataConversionException
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random


class RandomFunctionExecutor : FunctionValueExecutor {


    private val random = Random(System.currentTimeMillis())

    override fun execute(name: String, args: Array<Any?>): Any {
        if (args.size >= 2) {
            val min = if (args[0]?.toString()?.isEmpty() == true) "0" else args[0].toString()
            val max = args[1]?.toString() ?: "0"
            return when (if (args.size == 3) args[2]?.toString() ?: "" else "") {
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

    private fun randomBigDecimal(min: BigDecimal, max: BigDecimal): BigDecimal {
        val randomBigDecimal = min.add(BigDecimal(random.nextDouble()).multiply(max.subtract(min)))
        return randomBigDecimal.setScale(0, RoundingMode.HALF_UP)
    }

    override fun getFunctionName(): String = "rand"
}