package org.skellig.teststep.processor.web3.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.web3.model.Web3TestStep
import java.util.*

class Web3TestStepFactory(
    testStepRegistry: TestStepRegistry,
    keywordsProperties: Properties?,
    testStepFactoryValueConverter: TestStepFactoryValueConverter
) : BaseDefaultTestStepFactory<Web3TestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        private const val NODES_KEYWORD = "test.step.keyword.nodes"
        private const val METHOD_KEYWORD = "test.step.keyword.method"
        private const val PROTOCOL_KEYWORD = "test.step.keyword.protocol"
        private const val PARAMS_KEYWORD = "test.step.keyword.params"
        private const val RETURNS_KEYWORD = "test.step.keyword.returns"
        private const val EVENT_KEYWORD = "test.step.keyword.event"
        private const val FILTER_KEYWORD = "test.step.keyword.filter"
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<Web3TestStep> {
        val nodes = getStringArrayDataFromRawTestStep(getKeywordName(NODES_KEYWORD, "nodes"), rawTestStep, parameters)
        return Web3TestStep.Builder()
            .withNodes(nodes)
            .withMethod(getMethod(rawTestStep, parameters))
            .withEvent(getEvent(rawTestStep, parameters))
            .withFilter(getFilter(rawTestStep, parameters))
            .withParams(getParams(rawTestStep, parameters))
            .withReturns(getReturns(rawTestStep, parameters))
    }

    private fun getMethod(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): String? =
        convertValue(rawTestStep[getKeywordName(METHOD_KEYWORD, "method")], parameters)

    private fun getEvent(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): String? =
        convertValue(rawTestStep[getKeywordName(EVENT_KEYWORD, "event")], parameters)

    private fun getFilter(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Map<String, Any>? =
        convertValue(rawTestStep[getKeywordName(FILTER_KEYWORD, "filter")], parameters)

    private fun getParams(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Any? =
        convertValue(rawTestStep[getKeywordName(PARAMS_KEYWORD, "params")], parameters)

    private fun getReturns(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): List<String>? =
        convertValue(rawTestStep[getKeywordName(RETURNS_KEYWORD, "returns")], parameters)

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean =
        rawTestStep[getKeywordName(PROTOCOL_KEYWORD, "protocol")] == "web3"

}