package org.skellig.teststep.processing.value.function.collection

import java.math.BigDecimal

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