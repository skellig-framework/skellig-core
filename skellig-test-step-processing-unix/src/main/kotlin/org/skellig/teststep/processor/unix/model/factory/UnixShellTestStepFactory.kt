package org.skellig.teststep.processor.unix.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory
import org.skellig.teststep.processor.unix.model.UnixShellTestStep
import java.util.*

class UnixShellTestStepFactory(keywordsProperties: Properties?,
                               testStepValueConverter: TestStepValueConverter?,
                               testDataConverter: TestDataConverter?)
    : BaseTestStepFactory(keywordsProperties, testStepValueConverter, testDataConverter) {

    companion object {
        private const val HOSTS_KEYWORD = "test.step.keyword.hosts"
        private const val COMMAND_KEYWORD = "test.step.keyword.command"
        private const val ARGS_KEYWORD = "test.step.keyword.args"
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): TestStep.Builder {
        val services = getStringArrayDataFromRawTestStep(getKeywordName(HOSTS_KEYWORD, "hosts"), rawTestStep, parameters)
        return UnixShellTestStep.Builder()
                .withHosts(services)
                .withCommand(convertValue(rawTestStep[commandKeyword], parameters))
                .withArgs(convertValue(rawTestStep[getKeywordName(ARGS_KEYWORD, "args")], parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey(commandKeyword)
    }

    private val commandKeyword: String = getKeywordName(COMMAND_KEYWORD, "command")
}