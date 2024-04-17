package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the 'findAll' function on a [Collection] where items are found based on provided predicate.
 *
 * Supported args:
 * - findAll(`<predicate>`) - where `<predicate>` is a Boolean lambda expression, for example: findAll(i -> i.price > 0)*
 */
class FindAllFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.filter(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "findAll"

}