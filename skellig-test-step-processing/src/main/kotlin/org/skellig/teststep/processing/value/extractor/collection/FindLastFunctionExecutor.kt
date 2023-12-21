package org.skellig.teststep.processing.value.extractor.collection

class FindLastFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.findLast(getPredicate(lambdaFunction))
    }

    override fun getExtractFunctionName(): String = "findLast"

}