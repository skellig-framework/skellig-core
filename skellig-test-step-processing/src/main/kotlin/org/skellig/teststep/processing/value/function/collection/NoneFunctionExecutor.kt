package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the 'none' function on a collection of items.
 * The `none` function returns `true` if none of items in the collection satisfies the given predicate,
 * otherwise it returns `false`.
 *
 * Supported args:
 * - none(`<predicate>`) - where `<predicate>` is a Boolean lambda expression, for example: none(i -> i.price > 0)
 */
class NoneFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.none(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "none"

}