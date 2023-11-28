package org.skellig.teststep.processor.cassandra.model.factory

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processor.cassandra.model.CassandraTestStep
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import org.skellig.teststep.processor.db.model.factory.DatabaseTestStepFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

class CassandraTestStepFactory(
    testStepRegistry: TestStepRegistry,
    valueExpressionContextFactory: ValueExpressionContextFactory
) : DatabaseTestStepFactory<CassandraTestStep>(testStepRegistry, valueExpressionContextFactory) {

    companion object {
        private val CASSANDRA = AlphanumericValueExpression("cassandra")
    }

    override fun createDatabaseTestStepBuilder(rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>):
            DatabaseTestStep.Builder<CassandraTestStep> = CassandraTestStep.Builder()

    override fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean =
        rawTestStep[PROPERTY_PROVIDER_KEYWORD] == CASSANDRA && super.isConstructableFrom(rawTestStep)
}