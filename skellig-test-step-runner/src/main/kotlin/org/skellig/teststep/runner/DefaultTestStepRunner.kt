package org.skellig.teststep.runner

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class DefaultTestStepRunner private constructor(private val testStepProcessor: TestStepProcessor<TestStep>,
                                                         private val testStepsRegistry: TestStepRegistry,
                                                         private val testStepFactory: TestStepFactory<TestStep>) : TestStepRunner {

    companion object {
        private val LOGGER : Logger = LoggerFactory.getLogger(DefaultTestStepRunner.javaClass)
    }

    override fun run(testStepName: String): TestStepRunResult {
        return run(testStepName, emptyMap<String, String>())
    }

    override fun run(testStepName: String, parameters: Map<String, String?>): TestStepRunResult {
        val rawTestStep = testStepsRegistry.getByName(testStepName)

        return rawTestStep?.let {
            LOGGER.info("Run test step '$testStepName'")

            val testStep = testStepFactory.create(testStepName, rawTestStep, parameters)
            return testStepProcessor.process(testStep)
        } ?: error("Test step '${testStepName}' is not found in any of registered test data files in resources " +
                "or classes of the classloader")
    }

    class Builder {

        private var testStepProcessor: TestStepProcessor<TestStep>? = null
        private var testStepFactory: TestStepFactory<TestStep>? = null
        private var testStepsRegistry: TestStepRegistry? = null

        fun withTestStepsRegistry(testStepsRegistry: TestStepRegistry) = apply {
            this.testStepsRegistry = testStepsRegistry
        }

        fun withTestStepProcessor(testStepProcessor: TestStepProcessor<TestStep>) = apply {
            this.testStepProcessor = testStepProcessor
        }

        fun withTestStepFactory(testStepFactory: TestStepFactory<TestStep>) = apply {
            this.testStepFactory = testStepFactory
        }

        fun build(): TestStepRunner {
            return DefaultTestStepRunner(testStepProcessor ?: error("Test Step processor is mandatory"),
                    testStepsRegistry ?: error("Test Steps Registry is mandatory for Test Step Runner"),
                    testStepFactory ?: error("Test Step Factory is mandatory for Test Step Runner"))
        }
    }

}