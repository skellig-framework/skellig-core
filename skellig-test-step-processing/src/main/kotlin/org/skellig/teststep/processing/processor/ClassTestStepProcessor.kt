package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.util.info
import org.skellig.teststep.processing.util.logTestStepResult
import org.skellig.teststep.processing.util.logger
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Process [ClassTestStep] by invoking methods annotated with `@TestStep`.
 *
 * @see org.skellig.teststep.runner.annotation.TestStep
 */
internal class ClassTestStepProcessor(private val testScenarioState: TestScenarioState) : TestStepProcessor<ClassTestStep> {

    private val log = logger<ClassTestStepProcessor>()

    override fun process(testStep: ClassTestStep): TestStepProcessor.TestStepRunResult {
        return invoke(testStep.name, testStep, testStep.parameters)
    }

    override fun getTestStepClass(): Class<*> = ClassTestStep::class.java

    @Throws(TestStepProcessingException::class)
    operator fun invoke(
        testStepName: String,
        testStep: ClassTestStep,
        parameters: Map<String, Any?>?
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

        log.info(
            testStep,
            "Invoke method '${testStepDefInstance.javaClass}:$testStepMethod'" +
                    if (log.isDebugEnabled) "(${getParametersAsString(methodParameters)})" else ""
        )
        try {
            response = testStepMethod.invoke(testStepDefInstance, *methodParameters)
            testScenarioState.set(testStep.getId + TestStepProcessor.RESULT_SAVE_SUFFIX, response)
        } catch (e: IllegalAccessException) {
            error = TestStepProcessingException("Failed to access non-public method '${testStepDefInstance.javaClass.simpleName}:$testStepMethod'" +
                    " of test step '${testStep.name}'")
        } catch (e: InvocationTargetException) {
            var targetException: Throwable = e
            if (e.targetException != null) {
                targetException = e.targetException
            }
            error = TestStepProcessingException(targetException.message, targetException)
        } finally {
            log.logTestStepResult(testStep, result, error)
            result.notify(response, error)
        }
        return result
    }

    private fun getMethodParameters(
        testStepName: String,
        testStep: ClassTestStep,
        parameters: Map<String, Any?>?
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

    private fun getParametersAsString(methodParameters: Array<Any?>) =
        methodParameters.joinToString(",") { "$it: ${it?.javaClass?.simpleName ?: ""}" }
}
