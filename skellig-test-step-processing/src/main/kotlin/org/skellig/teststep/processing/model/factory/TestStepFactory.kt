package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.TestStep
import org.skellig.teststep.reader.value.expression.ValueExpression


/**
 * Factory interface for creating Test Step objects.
 * This interface is used in combination with [TestStepProcessor][org.skellig.teststep.processing.processor.TestStepProcessor]
 * to provide the latter with an appropriate [TestStep] for further processing.
 *
 * @param T The type of Test Step.
 */
interface TestStepFactory<T : TestStep> {

    /**
     * Creates a test step object from raw test step (parsed from a file or extracted from class).
     *
     * @param testStepName The name of the test step.
     * @param rawTestStep The raw test step data as a map of value expressions and their corresponding values.
     * @param parameters The parameters to be applied in the test step. Usually provided from a feature file, other test steps or directly from code.
     * @return The created test step object of type T, implementing [TestStep].
     */
    fun create(testStepName: String, rawTestStep: Map<ValueExpression, ValueExpression?>, parameters: Map<String, Any?>): T

    /**
     * Checks if this factory can be applied to construct an object from raw test step.
     * This method can be used in a Facade class which has a list of supported factories and needs to know which one to
     * apply in order to construct a test step from its raw equivalent.
     *
     * @param rawTestStep The raw test step data as a map of value expressions and their corresponding values.
     * @return `true` if the raw test step can be used to construct an object, `false` otherwise.
     */
    fun isConstructableFrom(rawTestStep: Map<ValueExpression, ValueExpression?>): Boolean
}