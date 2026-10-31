package org.skellig.teststep.processing.processor

import org.skellig.teststep.processing.exception.TestStepProcessingException
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.model.getScope
import org.skellig.teststep.processing.state.ScopeParameters
import org.skellig.teststep.processing.state.TestScenarioScope
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
internal class ClassTestStepProcessor : TestStepProcessor<ClassTestStep> {

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
            testStep.getScope().state.set(testStep.getId + TestStepProcessor.RESULT_SAVE_SUFFIX, response)
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
        val parameterTypes = testStepMethod.parameterTypes
        val matcher = testStep.testStepNamePattern.matcher(testStepName)
        val methodParameters = arrayOfNulls<Any>(parameterTypes.size)

        val regexGroups = mutableListOf<String?>()
        while (matcher.find()) {
            for (i in 1..matcher.groupCount()) {
                regexGroups.add(matcher.group(i))
            }
        }

        // the state and the scope go to the parameters of exactly these types, the rest are filled by regex groups in order.
        var nextRegexGroup = 0
        var lastNonInjectedIndex = -1
        parameterTypes.forEachIndexed { index, type ->
            if (ScopeParameters.isInjected(type)) {
                methodParameters[index] = ScopeParameters.valueFor(type, testStep.getScope())
            } else {
                lastNonInjectedIndex = index
                if (nextRegexGroup < regexGroups.size) methodParameters[index] = regexGroups[nextRegexGroup++]
            }
        }
        // if the last of the other parameters can take the Map, then it gets the parameters of the test step.
        if (lastNonInjectedIndex >= 0 && parameterTypes[lastNonInjectedIndex].isAssignableFrom(Map::class.java)) {
            // the scope is not for the user code, which only sees the parameters of the test step
            methodParameters[lastNonInjectedIndex] = parameters?.let { TestScenarioScope.removeFrom(it) }
        }
        return methodParameters
    }

    private fun getParametersAsString(methodParameters: Array<Any?>) =
        methodParameters.joinToString(",") { "$it: ${it?.javaClass?.simpleName ?: ""}" }
}
