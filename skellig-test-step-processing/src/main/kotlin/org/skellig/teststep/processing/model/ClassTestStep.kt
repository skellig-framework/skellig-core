package org.skellig.teststep.processing.model

import java.lang.reflect.Method
import java.util.regex.Pattern

class ClassTestStep(val testStepNamePattern: Pattern,
                    val testStepDefInstance: Any,
                    val testStepMethod: Method,
                    override val name: String,
                    val parameters: Map<String, String?>?) : TestStep