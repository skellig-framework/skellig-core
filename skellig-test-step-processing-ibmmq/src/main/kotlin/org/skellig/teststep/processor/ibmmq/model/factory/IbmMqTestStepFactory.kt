package org.skellig.teststep.processor.ibmmq.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * Factory class for creating IBMMQ test steps.
 *
 * @property testStepRegistry The test step registry used for storing and retrieving test steps.
 * @property valueExpressionContextFactory The value expression context factory used to create the value expression contexts
 * for evaluation of [ValueExpression].
 * @property defaultTestDataConverter The name of default converter (function) for test data (optional).
 */
open class IbmMqTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory,
    defaultTestDataConverter: String? = null
) : BaseIbmMqTestStepFactory<IbmMqTestStep>(testStepRegistry, valueExpressionContextFactory, defaultTestDataConverter) {

    companion object {
        private val SEND_TO_KEYWORD = fromProperty("sendTo")
        private val READ_FROM_KEYWORD = fromProperty("readFrom")
        private const val DEFAULT_DELAY = 250
        private const val DEFAULT_ATTEMPTS = 20
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<IbmMqTestStep> {
        return IbmMqTestStep.Builder()
            .sendTo(getSendToChannels(rawTestStep, parameters))
            .readFrom(getReadFromChannels(rawTestStep, parameters))
            .respondTo(getRespondToChannels(rawTestStep, parameters))
    }

    private fun getRespondToChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Set<String>? =
        toSet(convertValue<Any>(rawTestStep[RESPOND_TO_KEYWORD], parameters))

    private fun getSendToChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Set<String>? =
        toSet(convertValue<Any>(rawTestStep[SEND_TO_KEYWORD], parameters))

    private fun getReadFromChannels(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Set<String>? =
        toSet(convertValue<Any>(rawTestStep[READ_FROM_KEYWORD], parameters))

    private fun toSet(channel: Any?): Set<String>? {
        return when (channel) {
            is Collection<*> -> channel.map { it.toString() }.toSet()
            else -> channel?.let { setOf(channel.toString()) }
        }
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        !rawTestStep.containsKey(getConsumeFromKeyword()) && hasIbmMqRequiredData(rawTestStep)

    override fun getDelay(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        val delay = super.getDelay(rawTestStep, parameters)
        return if (delay == 0) DEFAULT_DELAY else delay
    }

    override fun getAttempts(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        val attempts = super.getAttempts(rawTestStep, parameters)
        return if (attempts == 0) DEFAULT_ATTEMPTS else attempts
    }

}