package org.skellig.teststep.processing.model.factory

import org.skellig.teststep.processing.model.ClassTestStep
import java.lang.reflect.Method
import java.util.regex.Pattern

internal class ClassTestStepFactory : TestStepFactory<ClassTestStep> {

    override fun create(testStepName: String, rawTestStep: Map<Any, Any?>, parameters: Map<String, String?>): ClassTestStep {
        return ClassTestStep(
                rawTestStep["id"] as String,
                rawTestStep["testStepNamePattern"] as Pattern,
                rawTestStep["testStepDefInstance"] ?: error("TestStepDefInstance must not be null"),
                rawTestStep["testStepMethod"] as Method,
                testStepName,
                parameters
        )
    }

    override fun isConstructableFrom(rawTestStep: Map<Any, Any?>): Boolean {
        return rawTestStep.containsKey("testStepNamePattern") &&
                rawTestStep.containsKey("testStepDefInstance") &&
                rawTestStep.containsKey("testStepMethod") &&
                rawTestStep["testStepMethod"]?.javaClass == Method::class.java
    }

}