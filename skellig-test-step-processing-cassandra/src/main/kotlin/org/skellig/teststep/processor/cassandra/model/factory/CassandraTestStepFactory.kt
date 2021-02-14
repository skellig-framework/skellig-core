package org.skellig.teststep.processor.cassandra.model.factory

import org.skellig.teststep.processing.converter.TestDataConverter
import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processor.cassandra.model.CassandraTestStep
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import org.skellig.teststep.processor.db.model.factory.DatabaseTestStepFactory
import java.util.*

class CassandraTestStepFactory(keywordsProperties: Properties?,
                               testStepValueConverter: TestStepValueConverter?,
                               testDataConverter: TestDataConverter?)
    : DatabaseTestStepFactory<CassandraTestStep>(keywordsProperties, testStepValueConverter, testDataConverter) {

    override fun createDatabaseTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>):
            DatabaseTestStep.Builder<CassandraTestStep> = CassandraTestStep.Builder()

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean =
            rawTestStep[getProviderKeyword()] == "cassandra" && super.isConstructableFrom(rawTestStep)
}