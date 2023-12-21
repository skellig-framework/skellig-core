package org.skellig.teststep.processing.value.extractor.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.skellig.teststep.processing.value.extractor.ValueExtractor

abstract class BaseCollectionFunctionExecutor : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return if (args.size == 1) {
            if (value is Collection<*> && args[0] is Function1<*, *>) {
                val lambdaFunction = args[0] as Function1<Any?, Any?>
                executeInternal(value, lambdaFunction)
            } else getInvalidValueOrArgTypeError(value?.javaClass)
        } else throw FunctionExecutionException("Failed to execute function '${getExtractFunctionName()}' as it must have 1 String argument but has ${args.size}")
    }

    protected abstract fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any?

    protected fun getInvalidLambdaResultType(expectedType: Class<*>, actualType: Class<*>?): String =
        "Unexpected result type returned when executing a predicate of the function '${getExtractFunctionName()}'\n" +
                "Expected '$expectedType' but was '$actualType'"

    protected open fun getInvalidValueOrArgTypeError(valueType: Class<*>?) {
        throw FunctionExecutionException(
            "Cannot execute function '${getExtractFunctionName()}' on $valueType as it's only allowed for a collection of items " +
                    "with an argument as a predicate"
        )
    }

}