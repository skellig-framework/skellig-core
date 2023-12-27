package org.skellig.teststep.processing.value.function.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

abstract class BaseBooleanFunctionExecutor : BaseCollectionFunctionExecutor() {

    protected fun getPredicate(lambdaFunction: (Any?) -> Any?): (Any?) -> Boolean {
        return {
            val result = lambdaFunction.invoke(it)
            (result as? Boolean) ?: throw FunctionExecutionException(getInvalidLambdaResultType(Boolean::class.java, result?.javaClass))
        }
    }

}