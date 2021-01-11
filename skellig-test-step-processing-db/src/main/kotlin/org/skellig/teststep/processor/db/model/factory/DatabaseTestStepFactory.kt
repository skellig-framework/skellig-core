package org.skellig.teststep.processor.db.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processing.exception.TestDataConversionException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import java.util.*

class DatabaseTestStepFactory(keywordsProperties: Properties?,
                              testStepValueConverter: TestStepValueConverter?,
                              testDataConverter: TestDataConverter?)
    : BaseTestStepFactory<DatabaseTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    companion object {
        private const val SERVERS_KEYWORD = "test.step.keyword.servers"
        private const val TABLE_KEYWORD = "test.step.keyword.table"
        private const val COMMAND_KEYWORD = "test.step.keyword.command"
        private const val QUERY_KEYWORD = "test.step.keyword.query"
        private const val WHERE_KEYWORD = "test.step.keyword.where"
        private const val VALUES_KEYWORD = "test.step.keyword.values"
    }

    private var dbTestDataKeywords = setOf(
            getKeywordName(WHERE_KEYWORD, "where"),
            getKeywordName(VALUES_KEYWORD, "values"))

    override fun create(testStepName: String, rawTestStep: Map<String, Any?>, parameters: Map<String, String?>): DatabaseTestStep {
        val testStep = super.create(testStepName, rawTestStep, parameters)
        if (testStep.testData != null && testStep.testData !is Map<*, *>) {
            throw TestDataConversionException("Test Data of Database Test Step must be class of Map<String,Object>")
        }
        return testStep
    }

    protected override fun createTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<DatabaseTestStep> {
        val servers = getStringArrayDataFromRawTestStep(getKeywordName(SERVERS_KEYWORD, "servers"), rawTestStep, parameters)
        return DatabaseTestStep.Builder()
                .withServers(servers!!)
                .withCommand(convertValue<String>(rawTestStep[getKeywordName(COMMAND_KEYWORD, "command")], parameters))
                .withTable(convertValue<String>(rawTestStep[getTableKeyword()], parameters))
                .withQuery(convertValue<String>(rawTestStep[getQueryKeyword()], parameters))
    }

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean {
        return rawTestStep.containsKey(getTableKeyword()) || rawTestStep.containsKey(getQueryKeyword())
    }

    override fun getTestDataKeywords(): Set<String> {
        return dbTestDataKeywords
    }

    private fun getQueryKeyword(): String {
        return getKeywordName(QUERY_KEYWORD, "query")
    }

    private fun getTableKeyword(): String {
        return getKeywordName(TABLE_KEYWORD, "table")
    }
}