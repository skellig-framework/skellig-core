package org.skellig.teststep.processor.unix.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.unix.model.UnixShellTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * The `UnixShellTestStepFactory` class is responsible for creating instances of the [UnixShellTestStep] class.
 *
 * @property testStepRegistry The test step registry used to retrieve test steps.
 * @property valueExpressionContextFactory The value expression context factory used to create the value expression contexts
 * for evaluation of [ValueExpression]
 */
class UnixShellTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<UnixShellTestStep>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        private val HOSTS_KEYWORD = fromProperty("hosts")
        private val COMMAND_KEYWORD = fromProperty("command")
        private val ARGS_KEYWORD = fromProperty("args")
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<UnixShellTestStep> {
        val services = getStringArrayDataFromRawTestStep(HOSTS_KEYWORD, rawTestStep, parameters)
        return UnixShellTestStep.Builder()
            .withHosts(services)
            .withCommand(convertValue(rawTestStep[COMMAND_KEYWORD], parameters))
            .withArgs(convertValue(rawTestStep[ARGS_KEYWORD], parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(HOSTS_KEYWORD) &&
                rawTestStep.containsKey(COMMAND_KEYWORD)
    }
}