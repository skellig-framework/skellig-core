package org.skellig.teststep.processing.value.extractor.collection

class NoneFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any {
        return value.none(getPredicate(lambdaFunction))
    }

    override fun getExtractFunctionName(): String = "none"

}