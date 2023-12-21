package org.skellig.teststep.processing.value.extractor.collection

class FindFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.find(getPredicate(lambdaFunction))
    }

    override fun getExtractFunctionName(): String = "find"

}