package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.processing.util.CachedPattern
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression

/**
 * BaseTestStepFactory is an abstract class that implements the TestStepFactory interface.
 * It provides common functionality and helper methods for creating TestStep objects.
 *
 * @param T The type of TestStep.
 * @property valueExpressionContextFactory A factory to create a ValueExpressionContext for evaluating [ValueExpression].
 */
abstract class BaseTestStepFactory<T : TestStep>(val valueExpressionContextFactory: ValueExpressionContextFactory) : TestStepFactory<T> {

    companion object {
        @JvmStatic
        protected fun fromProperty(property: String) : AlphanumericValueExpression = AlphanumericValueExpression(property)
        protected val TEST_STEP_NAME_KEYWORD = fromProperty("name")
    }

    protected open fun <T> convertValue(value: ValueExpression?, parameters: Map<String, Any?>): T? {
        return value?.evaluate(valueExpressionContextFactory.create(parameters)) as T?
    }

    /**
     * Extracts parameters from the test step name by matching it against a regular expression pattern.
     * If the regex pattern of a test step has groups, and you apply already formed name of the test step, then
     * it extract values from these groups and add them to the existing Map of parameters, where key is a respective group number,
     * and value is extracted value from the regex group. For example:
     *
     * If you have a test step in a file with this name: "User (.+) sends (\d+) coins"
     * and you run a test: "User Alex sends 100 coins", then you'll get these additional parameters:
     * - "1" - Alex
     * - "2"- 100
     *
     * which you can reference in the test step "User (.+) sends (\d+) coins" using expressions such as: ${1} or ${2}
     *
     * @param testStepName The name of the test step.
     * @param rawTestStep The raw test step, which is a map of value expressions and their corresponding values (nullable).
     * @return A mutable map of extracted parameters, where the key is the group number and the value is the extracted value.
     *         Returns null if no parameters were extracted. The Map is mutable because it may be modified within a test step.
     *         (see. TaskTestStepProcessor)
     */
    protected fun extractParametersFromTestStepName(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>): MutableMap<String, Any?>? {
        var parameters: MutableMap<String, Any?>? = null
        val matcher = CachedPattern.compile(getName(rawTestStep)).matcher(testStepName)
        if (matcher.find()) {
            parameters = mutableMapOf()
            for (i in 1..matcher.groupCount()) {
                val value = matcher.group(i)
                if (value.isNotEmpty()) {
                    parameters[i.toString()] = value
                }
            }
        }
        return parameters
    }

    /**
     * Returns the name from the raw test step.
     *
     * @param rawTestStep The raw test step.
     * @return The name of the test step as a string.
     */
    protected open fun getName(rawTestStep: Map<ValueExpression, ValueExpression?>): String {
        // No need to evaluate as it is always the value from toString
        return rawTestStep[TEST_STEP_NAME_KEYWORD].toString()
    }


}