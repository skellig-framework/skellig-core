package org.skellig.teststep.runner

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import java.net.URISyntaxException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

internal class DefaultTestStepRunner private constructor(private val testStepProcessor: TestStepProcessor<TestStep>,
                                                         private val testStepsRegistry: TestStepsRegistry,
                                                         private val classTestStepsRegistry: ClassTestStepsRegistry?,
                                                         private val testStepFactory: TestStepFactory) : TestStepRunner {

    private val testStepDefMethodRunner = TestStepDefMethodRunner()

    override fun run(testStepName: String): TestStepRunResult {
        return run(testStepName, emptyMap<String, String>())
    }

    override fun run(testStepName: String, parameters: Map<String, String?>): TestStepRunResult {
        val rawTestStep = testStepsRegistry.getByName(testStepName)

        return rawTestStep?.let {
            val testStep = testStepFactory.create(testStepName, rawTestStep, parameters)
            return testStepProcessor.process(testStep)
        } ?: run {
            val testStep = classTestStepsRegistry?.getTestStep(testStepName)

            testStep?.let {
                return testStepDefMethodRunner.invoke(testStepName, testStep, parameters)
            } ?: run {
                throw TestStepProcessingException(String.format("Test step '%s' is not found in any of registered test data files from: %s",
                        testStepName, testStepsRegistry.testStepsRootPath))
            }
        }
    }

    class Builder {

        private var testStepProcessor: TestStepProcessor<TestStep>? = null
        private var testStepReader: TestStepReader? = null
        private var classLoader: ClassLoader? = null
        private var testStepPaths: Collection<String>? = null
        private var testStepFactory: TestStepFactory? = null

        fun withTestStepProcessor(testStepProcessor: TestStepProcessor<TestStep>) = apply {
            this.testStepProcessor = testStepProcessor
        }

        fun withTestStepFactory(testStepFactory: TestStepFactory) = apply {
            this.testStepFactory = testStepFactory
        }

        fun withTestStepReader(testStepReader: TestStepReader, classLoader: ClassLoader, testStepPaths: Collection<String>) = apply {
            this.testStepReader = testStepReader
            this.classLoader = classLoader
            this.testStepPaths = testStepPaths
        }

        fun build(): TestStepRunner {
            val testStepPaths = extractTestStepPaths()
            val testStepClassPaths = extractTestStepPackages()
            val testStepsRegistry = TestStepsRegistry(TestStepFileExtension.STS, testStepReader
                    ?: error("Test Step Reader is mandatory"))
            testStepsRegistry.registerFoundTestStepsInPath(testStepPaths)

            val classTestStepsRegistry = ClassTestStepsRegistry(testStepClassPaths, classLoader!!)

            return DefaultTestStepRunner(testStepProcessor ?: error("Test Step processor is mandatory"),
                    testStepsRegistry, classTestStepsRegistry,
                    testStepFactory ?: error("Test Step Factory is mandatory"))
        }

        private fun extractTestStepPackages(): Collection<String> {
            return testStepPaths!!
                    .filter { !it.contains("/") }
                    .toSet()
        }

        private fun extractTestStepPaths(): Collection<Path> {
            return testStepPaths!!
                    .filter { !it.contains(".") }
                    .map {
                        try {
                            val resource = classLoader!!.getResource(it)
                            return@map Paths.get(resource!!.toURI())
                        } catch (e: URISyntaxException) {
                            throw TestStepRegistryException(e.message, e)
                        }
                    }
                    .toList()
        }
    }

}