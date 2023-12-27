package org.skellig.teststep.processing.value.function.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class SortFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any =
        value.sortedBy {
            val result = lambdaFunction.invoke(it)
            (result as? Comparable<Any?>) ?: throw FunctionExecutionException(getInvalidLambdaResultType(Comparable::class.java, result?.javaClass))
        }

    override fun getFunctionName(): String = "sort"

}