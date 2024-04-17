package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the "map" function on a collection of items using the provided lambda function. It converts all items of collection
 * based on provided lambda expression and returns a new collection.
 *
 * Supported args:
 * - map(`<lambda>`) - where `<lambda>` is a lambda expression which returns a new mapped value, for example: map(i -> i.price > 0)
 */
class MapFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any = value.map { lambdaFunction.invoke(it) }

    override fun getFunctionName(): String = "map"

}