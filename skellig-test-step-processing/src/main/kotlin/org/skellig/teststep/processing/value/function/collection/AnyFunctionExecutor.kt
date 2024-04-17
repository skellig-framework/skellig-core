package org.skellig.teststep.processing.value.function.collection


/**
 * Executes the 'any' function on a collection of items.
 * The `any` function returns `true` if at least one item in the collection satisfies the given predicate,
 * otherwise it returns `false`.
 *
 * Supported args:
 * - any(`<predicate>`) - where `<predicate>` is a Boolean lambda expression, for example: any(i -> i.price > 0)
 */
class AnyFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.any(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "any"

}