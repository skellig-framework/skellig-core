package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the 'find' function on a collection of items and finds first one which satisfies the provided predicate.
 *
 * Supported args:
 * - find(`<predicate>`) - where `<predicate>` is a Boolean lambda expression, for example: find(i -> i.price > 0)
 */
class FindFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.find(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "find"

}