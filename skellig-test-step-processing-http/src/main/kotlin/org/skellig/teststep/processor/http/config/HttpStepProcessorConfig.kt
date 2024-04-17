package org.skellig.teststep.processor.http.config

import com.typesafe.config.Config
import org.skellig.teststep.processing.processor.config.ConfiguredTestStepProcessorDetails
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfig
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.http.HttpTestStepProcessor
import org.skellig.teststep.processor.http.model.HttpTestStep
import org.skellig.teststep.processor.http.model.factory.HttpTestStepFactory

private const val HTTP_TEST_DATA_CONVERTER = "http.testData.converter"

/**
 * This class is responsible for configuring the HTTP test step processor.
 * A default data converter (ex. 'toJson') can be defined in 'http.testData.converter' property of Skellig [Config].
 */
class HttpTestStepProcessorConfig : TestStepProcessorConfig<HttpTestStep> {
    override fun config(details: TestStepProcessorConfigDetails): ConfiguredTestStepProcessorDetails<HttpTestStep>? {
        return if (details.config.hasPath("http"))
            ConfiguredTestStepProcessorDetails(
                HttpTestStepProcessor.Builder()
                    .withHttpService(details.config)
                    .withTestScenarioState(details.state)
                    .build(),
                HttpTestStepFactory(
                    details.testStepRegistry,
                    details.valueExpressionContextFactory,
                    getDefaultTestDataConverter(details.config)
                )
            )
        else null
    }

    private fun getDefaultTestDataConverter(config: Config): String? {
        return if (config.hasPath(HTTP_TEST_DATA_CONVERTER)) config.getString(HTTP_TEST_DATA_CONVERTER)
        else null
    }

}