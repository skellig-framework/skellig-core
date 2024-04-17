package org.skellig.teststep.processing.value.function.collection

import java.math.BigDecimal

/**
 * A class that calculates the minimum value of a given collection using a lambda function.
 * Returns [BigDecimal] as result.
 *
 * Supported args:
 * - minOf(`<lambda>`) - where `<lambda>` is a lambda expression to return a property to find min value, for example: minOf(i -> i.price)
 */
class MinOfFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.minOf {
            when (val result = lambdaFunction.invoke(it)) {
                is BigDecimal -> result
                is Int -> result.toBigDecimal()
                is Double -> result.toBigDecimal()
                else -> result.toString().toBigDecimal()
            }
        }
    }

    override fun getFunctionName(): String = "minOf"

}