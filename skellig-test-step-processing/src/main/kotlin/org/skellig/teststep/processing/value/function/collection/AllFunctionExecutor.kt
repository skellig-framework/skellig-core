package org.skellig.teststep.processing.value.function.collection

class AllFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.all(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "all"

}