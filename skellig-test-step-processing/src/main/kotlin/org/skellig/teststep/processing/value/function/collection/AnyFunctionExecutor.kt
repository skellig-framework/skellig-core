package org.skellig.teststep.processing.value.function.collection


class AnyFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.any(getPredicate(lambdaFunction))
    }

    override fun getFunctionName(): String = "any"

}