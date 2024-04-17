package org.skellig.teststep.processing.value.function.collection

import java.math.BigDecimal

/**
 * Executes the 'sumOf' function on a collection of values according to the provided predicate.
 * The result is [BigDecimal] as sum of all items
 *
 * Supported args:
 * - sumOf(`<lambda>`) - where `<lambda>` is a lambda expression to return a property to sum values, for example: sumOf(i -> i.price)
 */
class SumOfFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.sumOf {
            when (val result = lambdaFunction.invoke(it)) {
                is BigDecimal -> result
                is Int -> result.toBigDecimal()
                is Double -> result.toBigDecimal()
                else -> result.toString().toBigDecimal()
            }
        }
    }

    override fun getFunctionName(): String = "sumOf"

}