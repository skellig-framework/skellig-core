package org.skellig.teststep.processor.cassandra.config

import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.cassandra.CassandraTestStepProcessor
import org.skellig.teststep.processor.cassandra.model.CassandraTestStep
import org.skellig.teststep.processor.cassandra.model.factory.CassandraTestStepFactory
/**
 * This class is responsible for configuring a [CassandraTestStepProcessor] with the given [TestStepProcessorConfigDetails].
 */
class CassandraTestStepProcessorConfig : TestStepProcessorConfig<CassandraTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<CassandraTestStep>? {
        return if (details.config.hasPath("cassandra"))
            ConfiguredTestStepProcessorDetails(
                CassandraTestStepProcessor.Builder()
                    .withDbServers(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                CassandraTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory
                )
            )
        else null
    }
}