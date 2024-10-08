package org.skellig.teststep.processing.value.function

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random


/**
  * Executes a 'random' function and returns a random value based on the provided arguments.
 *
 * Supported args:
 * - random(`<min>`, `<max>`, `<type>`) - generates a random value with type (int, long, double or bigDecimal) within `<min>` and `<max>` range.
 * - random(`<min>`, `<max>`) - generates a random value with type 'long' within `<min>` and `<max>` range.
 */
class RandomFunctionExecutor : FunctionValueExecutor {


    private val random = Random(System.currentTimeMillis())

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any {
        if (value != null) throw FunctionExecutionException("Function `${getFunctionName()}` cannot be called from another value")

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
            throw FunctionExecutionException("Function `${getFunctionName()}` can only accept between 2 and 3 String arguments. Found ${args.size}")
        }
    }

    private fun randomBigDecimal(min: BigDecimal, max: BigDecimal): BigDecimal {
        val randomBigDecimal = min.add(BigDecimal(random.nextDouble()).multiply(max.subtract(min)))
        return randomBigDecimal.setScale(0, RoundingMode.HALF_UP)
    }

    override fun getFunctionName(): String = "random"
}