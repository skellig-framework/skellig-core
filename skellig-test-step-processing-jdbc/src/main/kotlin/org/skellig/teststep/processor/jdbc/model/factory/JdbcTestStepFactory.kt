package org.skellig.teststep.processor.jdbc.model.factory

import org.skellig.teststep.processing.converter.TestStepValueConverter
import org.skellig.teststep.processor.db.model.DatabaseTestStep
import org.skellig.teststep.processor.db.model.factory.DatabaseTestStepFactory
import org.skellig.teststep.processor.jdbc.model.JdbcTestStep
import java.util.*

class JdbcTestStepFactory(keywordsProperties: Properties?,
                          testStepValueConverter: TestStepValueConverter?)
    : DatabaseTestStepFactory<JdbcTestStep>(keywordsProperties, testStepValueConverter) {

    override fun createDatabaseTestStepBuilder(rawTestStep: Map<String, Any?>, parameters: Map<String, Any?>):
            DatabaseTestStep.Builder<JdbcTestStep> = JdbcTestStep.Builder()

    override fun isConstructableFrom(rawTestStep: Map<String, Any?>): Boolean =
            (rawTestStep[getProviderKeyword()] == "jdbc" || !rawTestStep.containsKey(getProviderKeyword())) &&
                    super.isConstructableFrom(rawTestStep)
}