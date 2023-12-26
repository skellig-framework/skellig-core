package org.skellig.runner.config.function

import org.skellig.teststep.processing.value.config.FunctionsConfig
import org.skellig.teststep.processing.value.config.FunctionsConfigDetails
import org.skellig.teststep.processing.value.extractor.ValueExtractor
import org.skellig.teststep.processing.value.function.FunctionValueExecutor


class CustomFormatFunctionValueExecutor : FunctionValueExecutor {

    companion object {
        private const val RAW_DATA = "rawData"
    }

    override fun execute(name: String, args: Array<Any?>): Any {
        val argAsMap = args[0] as Map<*, *>
        return if (!argAsMap.containsKey(RAW_DATA) && argAsMap.containsKey("id")) {
            argAsMap.plus(Pair(RAW_DATA, "${argAsMap["id"]}: ${argAsMap["userName"]} (${argAsMap["score"]})"))
        } else argAsMap
    }

    override fun getFunctionName(): String = "toCustomFormat"
}

class DefaultFunctionsConfig : FunctionsConfig {

    override fun configValueExtractors(details: FunctionsConfigDetails): List<ValueExtractor> = emptyList()

    override fun configFunctionExecutors(details: FunctionsConfigDetails): List<FunctionValueExecutor> =
        listOf(CustomFormatFunctionValueExecutor())

}