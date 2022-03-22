package org.skellig.teststep.processor.unix.model.factory

import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.unix.model.UnixShellTestStep
import java.util.*

class UnixShellTestStepFactory(testStepRegistry: TestStepRegistry,
                               keywordsProperties: Properties?,
                               testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<UnixShellTestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        private const val HOSTS_KEYWORD = "test.step.keyword.hosts"
        private const val COMMAND_KEYWORD = "test.step.keyword.command"
        private const val ARGS_KEYWORD = "test.step.keyword.args"
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<UnixShellTestStep> {
        val services = getStringArrayDataFromRawTestStep(getHostsKeyword(), rawTestStep, parameters)
        return UnixShellTestStep.Builder()
                .withHosts(services)
                .withCommand(convertValue(rawTestStep[getCommandKeyword()], parameters))
                .withArgs(convertValue(rawTestStep[getKeywordName(ARGS_KEYWORD, "args")], parameters))
    }

    private fun getCommandKeyword() = getKeywordName(COMMAND_KEYWORD, "command")

    private fun getHostsKeyword() = getKeywordName(HOSTS_KEYWORD, "hosts")

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey(getHostsKeyword()) &&
                rawTestStep.containsKey(getCommandKeyword())
    }
}