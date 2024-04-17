package org.skellig.teststep.processing.value.function.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

/**
 * Executes 'sort' function on a collection of items which implements [Comparable].
 * Returns a sorted collection.
 *
 * Supported args:
 * - sort(`<lambda>`) - where `<lambda>` is a lambda expression to return a property to sort values from, for example: sort(i -> i.price)
 */
class SortFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any =
        value.sortedBy {
            val result = lambdaFunction.invoke(it)
            (result as? Comparable<Any?>) ?: throw FunctionExecutionException(getInvalidLambdaResultType(Comparable::class.java, result?.javaClass))
        }

    override fun getFunctionName(): String = "sort"

}