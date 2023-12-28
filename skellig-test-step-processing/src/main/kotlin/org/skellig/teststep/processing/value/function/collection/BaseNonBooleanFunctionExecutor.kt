package org.skellig.teststep.processing.value.function.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

abstract class BaseNonBooleanFunctionExecutor : BaseCollectionFunctionExecutor() {

    override fun getInvalidValueOrArgTypeError(valueType: Class<*>?) {
        throw FunctionExecutionException(
            "Cannot execute function '${getFunctionName()}' on $valueType as it's only allowed for a collection of items " +
                    "with an argument as a function '(Any) -> Any'"
        )
    }

}