package org.skellig.teststep.processor.jdbc.model.factory

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import org.skellig.teststep.processor.db.model.factory.DatabaseTestStepFactory
import org.skellig.teststep.processor.jdbc.model.JdbcTestStep
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * JdbcTestStepFactory is a class that extends the [DatabaseTestStepFactory]
 * and is responsible for creating instances of [JdbcTestStep], if 'provider' property  of raw test step is 'jdbc'.
 *
 * @param testStepRegistry the registry for storing and retrieving test steps
 * @property valueExpressionContextFactory the factory for creating value expression context, used in evaluation of [ValueExpression]
 */
class JdbcTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : DatabaseTestStepFactory<JdbcTestStep>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        private val JDBC = fromProperty("jdbc")
    }

    override fun createDatabaseTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>):
            DatabaseTestStep.Builder<JdbcTestStep> = JdbcTestStep.Builder()

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean {
        return (rawTestStep[PROPERTY_PROVIDER_KEYWORD] == JDBC || !rawTestStep.containsKey(PROPERTY_PROVIDER_KEYWORD)) &&
                super.isConstructableFrom(rawTestStep)
    }
}