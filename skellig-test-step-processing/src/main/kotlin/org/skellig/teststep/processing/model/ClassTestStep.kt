package org.skellig.teststep.processing.model

import org.skellig.teststep.processing.util.PropertyFormatUtils
import java.lang.reflect.Method
import java.util.regex.Pattern

/**
 * A test step which has an implementation as a method in a class.
 */
class ClassTestStep(
    val id: String,
    val testStepNamePattern: Pattern,
    val testStepDefInstance: Any,
    val testStepMethod: Method,
    override val name: String,
    val parameters: Map<String, String?>?
) : TestStep {

    val getId: String = id
        get() = field.ifEmpty { name }


    override fun toString(): String {
        return "test step instance = ${testStepDefInstance.javaClass.simpleName}\n" +
                "method name = ${testStepMethod.name}\n" +
                (parameters?.let { if(it.isNotEmpty()) PropertyFormatUtils.toStringCollection(it.entries, "parameters {", "}", 0) else "" } ?: "")
    }
}