package org.skellig.teststep.processor.rmq.model.factory

import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.rmq.model.BaseRmqTestStep
import java.util.*

abstract class BaseRmqTestStepFactory<T : BaseRmqTestStep>(testStepRegistry: TestStepRegistry,
                                                           keywordsProperties: Properties?,
                                                           testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<T>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        internal const val PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol"
        internal const val ROUTING_KEY_KEYWORD = "test.step.keyword.routingKey"
        internal const val RESPOND_TO_KEYWORD = "test.step.keyword.respondTo"
        internal const val CONSUME_FROM_KEYWORD = "test.step.keyword.consumeFrom"
        internal const val RMQ_PROPERTIES_KEYWORD = "test.step.keyword.rmq.properties"
        internal const val RMQ = "rmq"
    }

    protected fun getRoutingKey(rawTestStep: Map<String, Any?>): Any? =
        rawTestStep[getKeywordName(ROUTING_KEY_KEYWORD, "routingKey")]

    protected fun getProperties(rawTestStep: Map<String, Any?>): Any? =
        rawTestStep[getKeywordName(RMQ_PROPERTIES_KEYWORD, "properties")]

    internal fun hasRmqRequiredData(rawTestStep: Map<String, Any?>): Boolean =
        getRoutingKey(rawTestStep) != null ||
                rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "") == RMQ

    internal fun getConsumeFromKeyword() = getKeywordName(CONSUME_FROM_KEYWORD, "consumeFrom")
}