package org.skellig.teststep.processing.value.extractor.collection

class MapFunctionExecutor : BaseNonBooleanFunctionExecutor() {

    override fun executeInternal(value: Collection<*>, lambdaFunction: (Any?) -> Any?): Any = value.map { lambdaFunction.invoke(it) }

    override fun getExtractFunctionName(): String = "map"

}