package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

internal class ClassTestStepProcessor : TestStepProcessor<ClassTestStep> {

    override fun process(testStep: ClassTestStep): TestStepProcessor.TestStepRunResult {
        return invoke(testStep.name, testStep, testStep.parameters)
    }

    override fun getTestStepClass(): Class<*> = ClassTestStep::class.java

    @Throws(TestStepProcessingException::class)
    operator fun invoke(testStepName: String, testStep: ClassTestStep, parameters: Map<String, String?>?): TestStepProcessor.TestStepRunResult {
        val methodParameters = getMethodParameters(testStepName, testStep, parameters)

        return invokeMethod(testStep, testStep.testStepMethod, methodParameters)
    }

    private fun invokeMethod(testStep: ClassTestStep,
                             testStepMethod: Method,
                             methodParameters: Array<Any?>): TestStepProcessor.TestStepRunResult {
        val result = TestStepProcessor.TestStepRunResult(testStep)
        val testStepDefInstance = testStep.testStepDefInstance
        var response: Any? = null
        var error: TestStepProcessingException? = null

        try {
            response = testStepMethod.invoke(testStepDefInstance, *methodParameters)
        } catch (e: IllegalAccessException) {
            error = TestStepProcessingException("Unexpected failure when running a test step method", e)
            throw error
        } catch (e: InvocationTargetException) {
            var targetException: Throwable = e
            if (e.targetException != null) {
                targetException = e.targetException
            }
            error = TestStepProcessingException(targetException.message, targetException)
        } finally {
            result.notify(response, error)
        }
        return result
    }

    private fun getMethodParameters(testStepName: String, testStep: ClassTestStep, parameters: Map<String, String?>?): Array<Any?> {
        val testStepMethod = testStep.testStepMethod
        val testStepNamePattern = testStep.testStepNamePattern
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
