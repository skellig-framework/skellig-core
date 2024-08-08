package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.DefaultTestStep

class TestStepProcessorResultTest {

    @Test
    fun testAwaitResultWithTimeoutWhenGotResult() {
        val defaultTestStep = mock<DefaultTestStep>()
        whenever(defaultTestStep.timeout).thenReturn(5000)

        val result = BaseTestStepProcessor.DefaultTestStepRunResult(defaultTestStep)
        var notifiedResponse: Any? = null
        var ex: RuntimeException? = null
        result.subscribe { _, r, e ->
            notifiedResponse = r
            ex = e
        }
        Thread {
            Thread.sleep(300)
            result.notify("response", null)
        }.start()

        result.awaitResult()

        assertEquals("response", notifiedResponse)
        assertNull(ex)
    }

    @Test
    fun testAwaitResultWhenGotError() {
        val defaultTestStep = mock<DefaultTestStep>()

        val result = BaseTestStepProcessor.DefaultTestStepRunResult(defaultTestStep)
        var ex: RuntimeException? = null
        result.subscribe { _, _, e ->
            ex = e
        }
        result.notify(null, RuntimeException("failed"))

        result.awaitResult()

        assertEquals("failed", ex?.message)
    }

    @Test
    fun testAwaitResultWithTimeoutWhenNoResult() {
        val defaultTestStep = mock<DefaultTestStep>()
        whenever(defaultTestStep.timeout).thenReturn(100)
        whenever(defaultTestStep.name).thenReturn("test A")

        val result = BaseTestStepProcessor.DefaultTestStepRunResult(defaultTestStep)

        val ex = assertThrows<TestStepProcessingException> { result.awaitResult() }
        assertEquals("Failed to received the final result of test step '${defaultTestStep.name}' within ${defaultTestStep.timeout} ms", ex.message)
    }
}