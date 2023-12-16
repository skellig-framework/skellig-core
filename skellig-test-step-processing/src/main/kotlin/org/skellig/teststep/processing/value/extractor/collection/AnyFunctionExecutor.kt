package org.skellig.teststep.processing.value.extractor.collection

import org.skellig.teststep.processing.value.exception.FunctionExecutionException

class AnyFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.any(getPredicate(lambdaFunction))
    }

    override fun getExtractFunctionName(): String = "any"

}