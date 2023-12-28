package org.skellig.teststep.processing.value.function.collection

class NoneFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.none(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "none"

}