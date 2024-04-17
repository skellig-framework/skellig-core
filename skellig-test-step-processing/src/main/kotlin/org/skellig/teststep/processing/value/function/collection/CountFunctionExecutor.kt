package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the 'count' function on a collection of items which satisfy the provided predicate.
 *
 * Supported args:
 * - count(`<predicate>`) - where `<predicate>` is a Boolean lambda expression, for example: count(i -> i.price > 0)
 */
class CountFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any =
        value.count(getPredicate(lambdaFunction))

    override fun getFunctionName(): String = "count"

}