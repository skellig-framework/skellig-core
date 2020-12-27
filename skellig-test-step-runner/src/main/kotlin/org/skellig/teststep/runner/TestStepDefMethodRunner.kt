package org.skellig.teststep.runner

import org.skellig.teststep.processing.processor.TestStepProcessor.TestStepRunResult
import org.skellig.teststep.runner.ClassTestStepsRegistry.TestStepDefDetails
import org.skellig.teststep.runner.exception.TestStepMethodInvocationException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

internal class TestStepDefMethodRunner {

    @Throws(TestStepMethodInvocationException::class)
    operator fun invoke(testStepName: String, testStepDefDetails: TestStepDefDetails, parameters: Map<String, String?>): TestStepRunResult {
        val methodParameters = getMethodParameters(testStepName, testStepDefDetails, parameters)

        return invokeMethod(testStepDefDetails, testStepDefDetails.testStepMethod, methodParameters)
    }

    private fun invokeMethod(testStepDefDetails: TestStepDefDetails,
                             testStepMethod: Method,
                             methodParameters: Array<Any?>): TestStepRunResult {
        val result = TestStepRunResult(null)
        val testStepDefInstance = testStepDefDetails.testStepDefInstance
        var response: Any? = null
        var error: TestStepMethodInvocationException? = null

        try {
            response = testStepMethod.invoke(testStepDefInstance, *methodParameters)
        } catch (e: IllegalAccessException) {
            error = TestStepMethodInvocationException("Unexpected failure when running a test step method", e)
            throw error
        } catch (e: InvocationTargetException) {
            var targetException: Throwable = e
            if (e.targetException != null) {
                targetException = e.targetException
            }
            error = TestStepMethodInvocationException(targetException.message, targetException)
            throw error
        } finally {
            result.notify(response, error)
        }
        return result
    }

    private fun getMethodParameters(testStepName: String, testStepDefDetails: TestStepDefDetails, parameters: Map<String, String?>): Array<Any?> {
        val testStepMethod = testStepDefDetails.testStepMethod
        val testStepNamePattern = testStepDefDetails.testStepNamePattern
        val matcher = testStepNamePattern.matcher(testStepName)
        val methodParameters = arrayOfNulls<Any>(testStepMethod.parameterCount)

        var index = 0
        if (matcher.find() && matcher.groupCount() > 0) {
            index += 1
            methodParameters[index - 1] = matcher.group(index)
            if (matcher.groupCount() < testStepMethod.parameterCount) {
                methodParameters[index] = parameters
            }
        }
        return methodParameters
    }
}