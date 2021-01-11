package org.skellig.teststep.runner.context

import com.typesafe.config.ConfigValue
import org.skellig.teststep.processor.http.HttpTestStepProcessor
import org.skellig.teststep.processor.http.model.factory.HttpTestStepFactory
import java.util.*
import java.util.function.Function

class DefaultSkelligTestContext : SkelligTestContext() {

    companion object {
        private const val TEST_STEP_KEYWORD = "test.step.keyword"
    }

    override var testStepKeywordsProperties: Properties? = null

    init {
        config?.let {
            if (it.hasPath(TEST_STEP_KEYWORD)) {
                testStepKeywordsProperties = Properties()
                it.getObject(TEST_STEP_KEYWORD)
                        .forEach { key: String, value: ConfigValue ->
                            testStepKeywordsProperties!!.setProperty("$TEST_STEP_KEYWORD.$key", value.toString())
                        }
            }
        }
    }

    override val testStepProcessors: List<TestStepProcessorDetails>
        get() =
            config?.let {
                listOf(
                        TestStepProcessorDetails(
                                HttpTestStepProcessor.Builder()
                                        .withHttpService(it)
                                        .withTestScenarioState(getTestScenarioState())
                                        .withValidator(getTestStepResultValidator())
                                        .build(),
                                createTestStepFactoryFrom { props, c1, c2 -> HttpTestStepFactory(props, c1, c2) })
                )
            } ?: emptyList()

    override val propertyExtractorFunction: Function<String, String?>
        get() = Function { key: String? -> config?.getString(key) }
}