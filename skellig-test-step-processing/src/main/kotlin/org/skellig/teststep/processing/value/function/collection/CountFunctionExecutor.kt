package org.skellig.teststep.processing.value.function.collection

class CountFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any =
        value.count(getPredicate(lambdaFunction))

    override fun getFunctionName(): String = "count"

}