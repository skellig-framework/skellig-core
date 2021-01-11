package org.skellig.teststep.runner.context

import com.typesafe.config.Config
import com.typesafe.config.ConfigValue
import org.skellig.teststep.processor.http.HttpTestStepProcessor
import org.skellig.teststep.processor.http.model.factory.HttpTestStepFactory
import java.util.*
import java.util.function.Function

class DefaultSkelligTestContext(private val config: Config) : SkelligTestContext() {

    companion object {
        private const val TEST_STEP_KEYWORD = "test.step.keyword"
    }

    override var testStepKeywordsProperties: Properties? = null

    init {
        if (config.hasPath(TEST_STEP_KEYWORD)) {
            testStepKeywordsProperties = Properties()
            config.getObject(TEST_STEP_KEYWORD)
                    .forEach { key: String, value: ConfigValue ->
                        testStepKeywordsProperties!!.setProperty("$TEST_STEP_KEYWORD.$key", value.toString())
                    }
        }
    }

    override val testStepProcessors: List<TestStepProcessorDetails>
        get() = listOf(
                TestStepProcessorDetails(
                        HttpTestStepProcessor.Builder()
                                .withHttpService(config)
                                .withTestScenarioState(getTestScenarioState())
                                .withValidator(getTestStepResultValidator())
                                .build(),
                        createTestStepFactoryFrom { props, c1, c2 -> HttpTestStepFactory(props, c1, c2) })
        )

    override val propertyExtractorFunction: Function<String, String?>
        get() = Function { key: String? -> config.getString(key) }
}