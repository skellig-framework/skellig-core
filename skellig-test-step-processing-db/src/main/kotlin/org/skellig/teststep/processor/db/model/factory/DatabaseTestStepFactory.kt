package org.skellig.teststep.processor.db.model.factory

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import java.util.*

abstract class DatabaseTestStepFactory<TS : DatabaseTestStep>(keywordsProperties: Properties?,
                                                              testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : BaseDefaultTestStepFactory<TS>(keywordsProperties, testStepFactoryValueConverter) {

    companion object {
        private const val PROVIDER_KEYWORD = "test.step.keyword.db.provide"
        private const val SERVERS_KEYWORD = "test.step.keyword.servers"
        private const val TABLE_KEYWORD = "test.step.keyword.table"
        private const val COMMAND_KEYWORD = "test.step.keyword.command"
        private const val QUERY_KEYWORD = "test.step.keyword.query"
        private const val WHERE_KEYWORD = "test.step.keyword.where"
        private const val VALUES_KEYWORD = "test.step.keyword.values"
        private const val DEFAULT_DELAY = 300
        private const val DEFAULT_ATTEMPTS = 10
    }

    private var dbTestDataKeywords = setOf(
            getKeywordName(WHERE_KEYWORD, "where"),
            getKeywordName(VALUES_KEYWORD, "values"))

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): TS {
        val testStep = super.create(testStepName, rawTestStep, parameters)
        if (testStep.testData != null) {
            if (testStep.query != null && testStep.testData !is List<*>) {
                throw TestStepProcessingException("Test data for Database Test Step with query must be list of values")
            } else if (testStep.query == null && testStep.testData !is Map<*, *>) {
                throw TestStepProcessingException("Test data for Database Test Step with command and table" +
                                                          " must be column-value pairs")
            }
        }
        return testStep
    }

    override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<TS> {
        val servers = getStringArrayDataFromRawTestStep(getKeywordName(SERVERS_KEYWORD, "servers"), rawTestStep, parameters)
        return createDatabaseTestStepBuilder(rawTestStep, parameters)
                .withServers(servers)
                .withCommand(convertValue<String>(rawTestStep[getKeywordName(COMMAND_KEYWORD, "command")], parameters))
                .withTable(convertValue<String>(rawTestStep[getTableKeyword()], parameters))
                .withQuery(convertValue<String>(rawTestStep[getQueryKeyword()], parameters))
    }

    protected abstract fun createDatabaseTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DatabaseTestStep.Builder<TS>

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey(getTableKeyword()) || rawTestStep.containsKey(getQueryKeyword())
    }

    override fun getDelay(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Int {
        val delay = super.getDelay(rawTestStep, parameters)
        return if (delay == 0) DEFAULT_DELAY else delay
    }

    override fun getAttempts(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): Int {
        val attempts = super.getAttempts(rawTestStep, parameters)
        return if (attempts == 0) DEFAULT_ATTEMPTS else attempts
    }

    override fun getTestDataKeywords(): Set<String> = dbTestDataKeywords

    private fun getQueryKeyword(): String = getKeywordName(QUERY_KEYWORD, "query")

    private fun getTableKeyword(): String = getKeywordName(TABLE_KEYWORD, "table")

    protected fun getProviderKeyword(): String = getKeywordName(PROVIDER_KEYWORD, "provider")
}