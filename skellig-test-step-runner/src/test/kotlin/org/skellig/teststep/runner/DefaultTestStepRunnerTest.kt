package org.skellig.teststep.runner

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.model.factory.TestStepFactory
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.reader.TestStepReader

@DisplayName("Run test step")
internal class DefaultTestStepRunnerTest {

    private var testStepRunner: TestStepRunner? = null
    private var testStep: TestStep? = null
    private var testStepProcessor = Mockito.mock(TestStepProcessor::class.java) as TestStepProcessor<TestStep>
    private var testStepReader = Mockito.mock(TestStepReader::class.java)
    private var testStepFactory = Mockito.mock(TestStepFactory::class.java)

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
                .withTestStepFactory(testStepFactory!!)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepReader(testStepReader!!, javaClass.classLoader, listOf("wrong path"))
                .build()

        Assertions.assertThrows(TestStepProcessingException::class.java) { testStepRunner!!.run(testStepName) }

        Mockito.verifyZeroInteractions(testStepProcessor)
    }

    @Test
    @DisplayName("When step doesn't exist Then throw exception")
    fun testRunTestStepWhenNotExist() {
        initializeTestSteps("test1", emptyMap<String, String>())
        initializeTestStepRunner()

        Assertions.assertThrows(TestStepProcessingException::class.java) { testStepRunner!!.run("test2") }

        Mockito.verifyZeroInteractions(testStepProcessor)
    }

    private fun initializeTestSteps(testStepName: String, parameters: Map<String, String?>) {
        val rawTestStep = mapOf(Pair("name", testStepName))
        testStep = TestStep.Builder()
                .withId("t1")
                .withName(testStepName)
                .build()

        whenever(testStepFactory.create(testStepName, rawTestStep, parameters)).thenReturn(testStep)
        doReturn(listOf(rawTestStep)).whenever(testStepReader).read(argThat { true })
    }

    private fun initializeTestStepRunner() {
        testStepRunner = DefaultTestStepRunner.Builder()
                .withTestStepFactory(testStepFactory!!)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepReader(testStepReader!!, javaClass.classLoader, listOf("steps"))
                .build()
    }
}