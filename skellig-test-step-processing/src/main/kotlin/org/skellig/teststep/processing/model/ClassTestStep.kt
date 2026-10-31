package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.util.PropertyFormatUtils
import java.lang.reflect.Method
import java.util.regex.Pattern

/**
 * A test step which references to a method of a class.
 *
 * The step holds the [testStepDefClass] and not an instance of it: the method is called on the instance which belongs
 * to the parallel group of the [scope] the step runs in.
 */
class ClassTestStep(
    val id: String,
    val testStepNamePattern: Pattern,
    val testStepDefClass: Class<*>,
    val testStepMethod: Method,
    override val name: String,
    val parameters: Map<String, Any?>?,
    override val scope: TestScenarioScope? = TestScenarioScope.from(parameters)
) : TestStep {

    val getId: String = id
        get() = field.ifEmpty { name }

    override fun toString(): String {
        return "test step class = ${testStepDefClass.simpleName}\n" +
                "method name = ${testStepMethod.name}\n" +
                (parameters?.let { if(it.isNotEmpty()) PropertyFormatUtils.toStringCollection(it.entries, "parameters {", "}", 0) else "" } ?: "")
    }
}