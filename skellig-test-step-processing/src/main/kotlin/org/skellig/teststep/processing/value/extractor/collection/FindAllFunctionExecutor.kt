package org.skellig.teststep.processing.value.extractor.collection

class FindAllFunctionExecutor : BaseBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any? {
        return value.filter(getPredicate(lambdaFunction))
    }

    override fun getExtractFunctionName(): String = "findAll"

}