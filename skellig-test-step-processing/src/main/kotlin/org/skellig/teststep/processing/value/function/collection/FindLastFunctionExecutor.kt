package org.skellig.teststep.processing.value.function.collection

class FindLastFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.findLast(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "findLast"

}