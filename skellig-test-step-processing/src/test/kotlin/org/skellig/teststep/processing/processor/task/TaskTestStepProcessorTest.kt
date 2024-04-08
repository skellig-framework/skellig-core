package org.skellig.teststep.processing.processor.task

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.task.async.AsyncTaskUtils
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.TaskTestStep
import org.skellig.teststep.processing.model.TestStepExecutionType
import org.skellig.teststep.processing.processor.BaseTestStepProcessor
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.processing.value.ValueExpressionContextFactoryTest
import org.skellig.teststep.processing.value.function.DefaultFunctionValueExecutor
import org.skellig.teststep.processing.value.property.DefaultPropertyExtractor
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.FunctionCallExpression
import org.skellig.teststep.reader.value.expression.MapValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

class TaskTestStepProcessorTest {

    private val testScenarioState = mock<TestScenarioState>()
    private val valueExpressionContextFactory = ValueExpressionContextFactory(
        DefaultFunctionValueExecutor.Builder()
            .withTestScenarioState(testScenarioState)
            .withClassLoader(ValueExpressionContextFactoryTest::class.java.classLoader)
            .build(),
        DefaultPropertyExtractor(null)
    )

    @Test
    fun testRunTestsInAsync() {
        val taskProcessor = createTaskTestStepProcessor { _, _ ->
            val testStepRunResult = BaseTestStepProcessor.DefaultTestStepRunResult(mock())
            Thread.sleep(200)
            testStepRunResult.notify("response", null)

            return@createTaskTestStepProcessor testStepRunResult
        }

        taskProcessor.process(createTaskTestStep(30, createAsyncEachTask())).subscribe { _, _, e ->
            assertNull(e)
        }
    }


    @Test
    fun testRunTestsWithDelayedResponseWhenFailsOnTimeout() {
        val taskProcessor = createTaskTestStepProcessor { _, _ ->
            val testStepRunResult = BaseTestStepProcessor.DefaultTestStepRunResult(mock())
            Thread.sleep(100)
            testStepRunResult.notify("response", null)

            return@createTaskTestStepProcessor testStepRunResult
        }

        val ex = assertThrows<TestStepProcessingException> {
            taskProcessor.process(createTaskTestStep(5, createRunTestsTask(), TestStepExecutionType.ASYNC)).awaitResult()
        }
        assertEquals("Failed to received the final result of test step 'task' within 5 ms", ex.message)
    }

    @Test
    fun testRunTestsInAsyncWhenOneFailsByTimeout() {
        val taskProcessor = createTaskTestStepProcessor { n, _ ->
            val defaultTestStep = mock<DefaultTestStep>()
            whenever(defaultTestStep.timeout).thenReturn(100)
            whenever(defaultTestStep.name).thenReturn(n)
            val testStepRunResult = BaseTestStepProcessor.DefaultTestStepRunResult(defaultTestStep)
            when (n) {
                "test B" -> {
                    AsyncTaskUtils.runTaskAsync {
                        Thread.sleep(2000)
                        testStepRunResult.notify("response from test B", null)
                    }
                }

                else -> testStepRunResult.notify("response", null)
            }
            return@createTaskTestStepProcessor testStepRunResult
        }

        taskProcessor.process(createTaskTestStep(30, createAsyncEachTask())).subscribe { _, r, e ->
            assertEquals("Failed to received the final result of test step 'test B' within 100 ms", e?.message)
        }
    }

    @Test
    fun testRunTestsInAsyncInsideAsyncTestStepProcessingWhenOneFailsByTimeout() {
        val taskProcessor = createTaskTestStepProcessor { n, _ ->
            val defaultTestStep = mock<DefaultTestStep>()
            whenever(defaultTestStep.timeout).thenReturn(100)
            whenever(defaultTestStep.name).thenReturn(n)
            val testStepRunResult = BaseTestStepProcessor.DefaultTestStepRunResult(defaultTestStep)
            if (n != "test C") testStepRunResult.notify("response", null)

            return@createTaskTestStepProcessor testStepRunResult
        }

        val runResult = taskProcessor.process(createTaskTestStep(30, createAsyncEachTask(), TestStepExecutionType.ASYNC))
        Thread.sleep(1000)
        runResult.subscribe { _, _, e ->
            assertEquals("Failed to received the final result of test step 'test C' within 100 ms", e?.message)
        }

        // second way of doing it by using awaitResult()
        val ex = assertThrows<TestStepProcessingException> {
            taskProcessor.process(createTaskTestStep(30, createAsyncEachTask(), TestStepExecutionType.ASYNC)).awaitResult()
        }

        assertEquals("Failed to received the final result of test step 'task' within 30 ms", ex.message)

        val ex2 = assertThrows<TestStepProcessingException> {
            taskProcessor.process(createTaskTestStep(500, createAsyncEachTask(), TestStepExecutionType.ASYNC)).awaitResult()
        }

        assertEquals("Failed to received the final result of test step 'test C' within 100 ms", ex2.cause?.message)
    }

    private fun createAsyncEachTask(): MapValueExpression {
        return MapValueExpression(mapOf(Pair(AlphanumericValueExpression("asyncEach"), createRunTestsTask())))
    }

    private fun createRunTestsTask() = MapValueExpression(
        mapOf(
            Pair(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test A"))), MapValueExpression(mapOf())),
            Pair(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test B"))), MapValueExpression(mapOf())),
            Pair(FunctionCallExpression("runTest", arrayOf(AlphanumericValueExpression("test C"))), MapValueExpression(mapOf())),
        )
    )

    private fun createTaskTestStep(timeout: Int, task: ValueExpression?, executionType: TestStepExecutionType = TestStepExecutionType.SYNC) =
        TaskTestStep("", "task", executionType, timeout, 0, 0, null, task, null, null, mutableMapOf())

    private fun createTaskTestStepProcessor(runTestDelegate: (String, Map<String, Any?>?) -> TestStepProcessor.TestStepRunResult): TaskTestStepProcessor {
        return TaskTestStepProcessor(
            DefaultTaskProcessor(
                testScenarioState,
                { value, parameters -> value?.evaluate(valueExpressionContextFactory.create(parameters)) },
                runTestDelegate
            ), testScenarioState
        )
    }
}