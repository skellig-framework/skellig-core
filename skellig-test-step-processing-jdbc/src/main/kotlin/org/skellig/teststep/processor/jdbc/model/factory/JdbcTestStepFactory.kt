package org.skellig.teststep.processor.jdbc.model.factory

import org.skellig.teststep.processing.model.factory.TestStepFactoryValueConverter
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import org.skellig.teststep.processor.db.model.factory.DatabaseTestStepFactory
import org.skellig.teststep.processor.jdbc.model.JdbcTestStep
import java.util.*

class JdbcTestStepFactory(testStepRegistry: TestStepRegistry,
                          keywordsProperties: Properties?,
                          testStepFactoryValueConverter: TestStepFactoryValueConverter)
    : DatabaseTestStepFactory<JdbcTestStep>(testStepRegistry, keywordsProperties, testStepFactoryValueConverter) {

    override fun createDatabaseTestStepBuilder(rawTestStep: Map<Any, Any?>, parameters: Map<String, Any?>):
            DatabaseTestStep.Builder<JdbcTestStep> = JdbcTestStep.Builder()

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean =
            (rawTestStep[getProviderKeyword()] == "jdbc" || !rawTestStep.containsKey(getProviderKeyword())) &&
                    super.isConstructableFrom(rawTestStep)
}