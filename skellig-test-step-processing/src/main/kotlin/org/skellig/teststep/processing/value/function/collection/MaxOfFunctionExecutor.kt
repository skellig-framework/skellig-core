package org.skellig.teststep.processing.value.function.collection

import java.math.BigDecimal

/**
 * Executes the `maxOf` function on a collection of values, using the provided lambda function.
 * Returns [BigDecimal] as a result.
 *
 * Supported args:
 * - maxOf(`<lambda>`) - where `<lambda>` is a lambda expression to return a property to find max value, for example: maxOf(i -> i.price)
 */
class MaxOfFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.maxOf {
            when (val result = lambdaFunction.invoke(it)) {
                is BigDecimal -> result
                is Int -> result.toBigDecimal()
                is Double -> result.toBigDecimal()
                else -> result.toString().toBigDecimal()
            }
        }
    }

    override fun getFunctionName(): String = "maxOf"

}