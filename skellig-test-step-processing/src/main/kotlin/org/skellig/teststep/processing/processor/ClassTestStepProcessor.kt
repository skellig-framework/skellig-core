package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.state.TestScenarioState
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

internal class ClassTestStepProcessor(val testScenarioState: TestScenarioState) : TestStepProcessor<ClassTestStep> {

    override fun process(testStep: ClassTestStep): TestStepProcessor.TestStepRunResult {
        return invoke(testStep.name, testStep, testStep.parameters)
    }

    override fun getTestStepClass(): Class<*> = ClassTestStep::class.java

    @Throws(TestStepProcessingException::class)
    operator fun invoke(
        testStepName: String,
        testStep: ClassTestStep,
        parameters: Map<String, String?>?
    ): TestStepProcessor.TestStepRunResult {
        val methodParameters = getMethodParameters(testStepName, testStep, parameters)

        return invokeMethod(testStep, testStep.testStepMethod, methodParameters)
    }

    private fun invokeMethod(
        testStep: ClassTestStep,
        testStepMethod: Method,
        methodParameters: Array<Any?>
    ): TestStepProcessor.TestStepRunResult {
        val result = TestStepProcessor.TestStepRunResult(testStep)
        val testStepDefInstance = testStep.testStepDefInstance
        var response: Any? = null
        var error: TestStepProcessingException? = null

        try {
            response = testStepMethod.invoke(testStepDefInstance, *methodParameters)
            testScenarioState.set(testStep.getId + TestStepProcessor.RESULT_SAVE_SUFFIX, response)
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

    private fun getMethodParameters(
        testStepName: String,
        testStep: ClassTestStep,
        parameters: Map<String, String?>?
    ): Array<Any?> {
        val testStepMethod = testStep.testStepMethod
        val testStepNamePattern = testStep.testStepNamePattern
        val matcher = testStepNamePattern.matcher(testStepName)
        val methodParameters = arrayOfNulls<Any>(testStepMethod.parameterCount)

        var index = 0
        while (matcher.find()) {
            for (i in 1..matcher.groupCount()) {
                methodParameters[index++] = matcher.group(i)
            }
        }
        // if method has parameters then add the parameters map at the end of the list.
        if (testStepMethod.parameterCount > 0) {
            val lastParamType = testStepMethod.parameterTypes[testStepMethod.parameterCount - 1]
            if (lastParamType.isAssignableFrom(Map::class.java)) {
                methodParameters[testStepMethod.parameterCount - 1] = parameters
            }
        }
        return methodParameters
    }
}
