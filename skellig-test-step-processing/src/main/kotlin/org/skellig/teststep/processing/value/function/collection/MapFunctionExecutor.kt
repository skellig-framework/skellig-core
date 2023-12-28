package org.skellig.teststep.processing.value.function.collection

class MapFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any = value.map { lambdaFunction.invoke(it) }

    override fun getFunctionName(): String = "map"

}