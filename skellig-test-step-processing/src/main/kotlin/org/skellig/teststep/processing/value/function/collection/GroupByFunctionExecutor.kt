package org.skellig.teststep.processing.value.function.collection

/**
 * Executes the "groupBy" function on a collection of items, grouping them according to the provided lambda expression
 * and returns [Map].
 *
 * Supported args:
 * - groupBy(`<lambda>`) - where `<lambda>` is a lambda expression to return a property to group by, for example: groupBy(i -> i.name)
 */
class GroupByFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any = value.groupBy { lambdaFunction.invoke(it) }

    override fun getFunctionName(): String = "groupBy"

}