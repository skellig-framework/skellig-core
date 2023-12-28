package org.skellig.teststep.processing.value.function.collection

class GroupByFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any = value.groupBy { lambdaFunction.invoke(it) }

    override fun getFunctionName(): String = "groupBy"

}