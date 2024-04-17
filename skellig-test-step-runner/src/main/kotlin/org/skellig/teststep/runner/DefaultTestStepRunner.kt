package org.skellig.teststep.runner

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logger

/**
 * The DefaultTestStepRunner class is responsible for running test steps and returning their execution results.
 * It implements the TestStepRunner interface and provides methods for running test steps with or without parameters.
 * The process of running a test step involves the following steps:
 * 1. Find the test step by name in the TestStepRegistry.
 * 2. Convert the raw data of the test step into an instance of TestStep using the TestStepFactory.
 * 3. Use the TestStepProcessor to process and execute the TestStep, returning the result.
 *
 * @param testStepProcessor The TestStepProcessor implementation used to process and execute the test steps.
 * @param testStepsRegistry The TestStepRegistry implementation used to store and retrieve test steps.
 * @param testStepFactory The TestStepFactory implementation used to create instances of TestStep.
 *
 * @see TestStepRunner
 */
internal class DefaultTestStepRunner private constructor(private val testStepProcessor: TestStepProcessor<TestStep>,
                                                         private val testStepsRegistry: TestStepRegistry,
                                                         private val testStepFactory: TestStepFactory<TestStep>) : TestStepRunner {

    private val log = logger<DefaultTestStepRunner>()

    override fun run(testStepName: String): TestStepRunResult {
        return run(testStepName, emptyMap<String, String>())
    }

    override fun run(testStepName: String, parameters: Map<String, Any?>): TestStepRunResult {
        val rawTestStep = testStepsRegistry.getByName(testStepName)

        return rawTestStep?.let {
            val testStep = testStepFactory.create(testStepName, rawTestStep, parameters)
            log.info(testStep, "Run test step '$testStepName'")

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