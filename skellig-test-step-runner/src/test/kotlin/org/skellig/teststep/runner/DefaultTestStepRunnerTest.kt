package org.skellig.teststep.runner

import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.StringValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.runner.model.TestStepFileExtension
import org.skellig.teststep.runner.registry.TestStepsRegistry

@DisplayName("Run test step")
internal class DefaultTestStepRunnerTest {

    private var testStepRunner: TestStepRunner? = null
    private var testStep: TestStep? = null
    private var testStepProcessor = mock<TestStepProcessor<TestStep>>()
    private var testStepReader = mock<TestStepReader>()
    private var testStepFactory = mock<TestStepFactory<TestStep>>()

    @Test
    @DisplayName("When no parameters extracted from name")
    fun testRunTestStepWithoutParameters() {
        val testStepName = "test1"
        initializeTestSteps(testStepName, emptyMap<String, String>())
        initializeTestStepRunner()

        testStepRunner!!.run(testStepName)

        Mockito.verify(testStepProcessor).process(testStep!!)
    }

    @Test
    @DisplayName("When steps not found in the resources Then throw correct exception")
    fun testRunTestStepWhenNoFound() {
        val testStepName = "test1"
        initializeTestSteps(testStepName, emptyMap<String, String>())
        testStepRunner = DefaultTestStepRunner.Builder()
                .withTestStepFactory(testStepFactory)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepsRegistry(createTestStepsRegistry("wrong path"))
                .build()

        Assertions.assertThrows(IllegalStateException::class.java) { testStepRunner!!.run(testStepName) }

        Mockito.verifyNoInteractions(testStepProcessor)
    }

    @Test
    @DisplayName("When step doesn't exist Then throw exception")
    fun testRunTestStepWhenNotExist() {
        initializeTestSteps("test1", emptyMap<String, String>())
        initializeTestStepRunner()

        Assertions.assertThrows(IllegalStateException::class.java) { testStepRunner!!.run("test2") }

        Mockito.verifyNoInteractions(testStepProcessor)
    }

    private fun initializeTestSteps(testStepName: String, parameters: Map<String, String?>) {
        val rawTestStep = mapOf<ValueExpression, ValueExpression?>(Pair(AlphanumericValueExpression("name"), StringValueExpression(testStepName)))
        testStep = DefaultTestStep.DefaultTestStepBuilder()
                .withId("t1")
                .withName(testStepName)
                .build()

        whenever(testStepFactory.create(testStepName, rawTestStep, parameters)).thenReturn(testStep)
        doReturn(listOf(rawTestStep)).whenever(testStepReader).read(argThat { true })
    }

    private fun initializeTestStepRunner() {
        testStepRunner = DefaultTestStepRunner.Builder()
                .withTestStepFactory(testStepFactory)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepsRegistry(createTestStepsRegistry("steps"))
                .build()
    }

    private fun createTestStepsRegistry(testStepsPath: String): TestStepsRegistry {
        val testStepsRegistry = TestStepsRegistry(TestStepFileExtension.STS, testStepReader)
        val resource = javaClass.classLoader.getResource(testStepsPath)
        resource?.let {
            testStepsRegistry.registerFoundTestStepsInPath(listOf(it.toURI()))
        }
        return testStepsRegistry
    }
}