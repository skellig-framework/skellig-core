package org.skellig.teststep.processor.db.model.factory

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.factory.BaseDefaultTestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * Abstract class representing a factory for creating instances of [DatabaseTestStep].
 * It extends BaseDefaultTestStepFactory and implements methods specific to database test steps.
 * It uses the following properties from a raw test step:
 * - provider - DB provider name
 * - servers - list of DB services where a query is executed
 * - table - name of a table in DB. If schema is needed, then it can be attached as a prefix, for example: Schema_A.Table_1
 * - command - a command representing a SQL query and used in combination with 'table', 'where' and 'data' properties where applicable.
 * The supported commands are: insert, update, select
 * - where - a Map of properties and values used with command 'select' or 'update'
 * - data - a Map of properties and values used with command 'insert'
 * - query - a plain SQL query, if 'table' and 'command' are not defined
 *
 * @param TS the type parameter representing the specific subclass of [DatabaseTestStep]
 * @property testStepRegistry the registry for storing and retrieving test steps
 * @property valueExpressionContextFactory the factory for creating value expression context, used in evaluation of [ValueExpression]
 */
abstract class DatabaseTestStepFactory<TS : DatabaseTestStep>(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : BaseDefaultTestStepFactory<TS>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        private val SERVERS_KEYWORD = fromProperty("servers")
        private val TABLE_KEYWORD = fromProperty("table")
        private val COMMAND_KEYWORD = fromProperty("command")
        private val QUERY_KEYWORD = fromProperty("query")
        private val WHERE_KEYWORD = fromProperty("where")
        private val DATA_KEYWORD = fromProperty("data")
        val PROPERTY_PROVIDER_KEYWORD = fromProperty("provider")
        private const val DEFAULT_DELAY = 300

        private const val DEFAULT_ATTEMPTS = 10
        private var DB_TEST_DATA_KEYWORDS = setOf(
            WHERE_KEYWORD,
            DATA_KEYWORD
        )
    }

    override fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): TS {
        val testStep = super.create(testStepName, rawTestStep, parameters)
        if (testStep.testData != null) {
            if (testStep.query != null && testStep.testData !is List<*>) {
                throw TestStepProcessingException("Test data for Database Test Step with query must be list of values")
            } else if (testStep.query == null && testStep.testData !is Map<*, *>) {
                throw TestStepProcessingException(
                    "Test data for Database Test Step with command and table" +
                            " must be column-value pairs"
                )
            }
        }
        return testStep
    }

    override fun createTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DefaultTestStep.Builder<TS> {
        val servers = getStringArrayDataFromRawTestStep(SERVERS_KEYWORD, rawTestStep, parameters)
        return createDatabaseTestStepBuilder(rawTestStep, parameters)
            .withServers(servers)
            .withCommand(convertValue<String>(rawTestStep[COMMAND_KEYWORD], parameters))
            .withTable(convertValue<String>(rawTestStep[TABLE_KEYWORD], parameters))
            .withQuery(convertValue<String>(rawTestStep[QUERY_KEYWORD], parameters))
    }

    protected abstract fun createDatabaseTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): DatabaseTestStep.Builder<TS>

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return rawTestStep.containsKey(TABLE_KEYWORD) || rawTestStep.containsKey(QUERY_KEYWORD)
    }

    override fun getDelay(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        val delay = super.getDelay(rawTestStep, parameters)
        return if (delay == 0) DEFAULT_DELAY else delay
    }

    override fun getAttempts(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): Int {
        val attempts = super.getAttempts(rawTestStep, parameters)
        return if (attempts == 0) DEFAULT_ATTEMPTS else attempts
    }

    override fun getTestDataKeywords(): Set<ValueExpression> = DB_TEST_DATA_KEYWORDS
}