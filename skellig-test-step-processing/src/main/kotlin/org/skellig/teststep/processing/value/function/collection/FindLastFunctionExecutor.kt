package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the 'findLast' function on a collection of items and finds last one which satisfies the provided predicate.
 *
 * Supported args:
 * - findLast(`<predicate>`) - where `<predicate>` is a Boolean lambda expression, for example: findLast(i -> i.price > 0)
 */
class FindLastFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.findLast(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "findLast"

}