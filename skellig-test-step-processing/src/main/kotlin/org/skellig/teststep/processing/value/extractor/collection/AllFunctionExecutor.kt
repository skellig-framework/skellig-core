package org.skellig.teststep.processing.value.extractor.collection

class AllFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.all(getPredicate(lambdaFunction))
    }

    override fun getExtractFunctionName(): String = "all"

}