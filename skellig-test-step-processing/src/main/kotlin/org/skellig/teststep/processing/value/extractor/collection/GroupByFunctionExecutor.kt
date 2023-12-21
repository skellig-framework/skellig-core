package org.skellig.teststep.processing.value.extractor.collection

class GroupByFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any = value.groupBy { lambdaFunction.invoke(it) }

    override fun getExtractFunctionName(): String = "groupBy"

}