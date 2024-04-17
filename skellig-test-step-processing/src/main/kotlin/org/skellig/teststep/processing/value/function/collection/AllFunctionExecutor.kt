package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the `all` function on a collection of items.
 * The `all` function returns `true` if all items in the collection satisfy the given predicate,
 * otherwise it returns `false`.
 *
 * Supported args:
 * - all(`<predicate>`) - where `<predicate>` is a Boolean lambda expression, for example: all(i -> i.price > 0)
 */
class AllFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.all(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "all"

}