package org.skellig.teststep.runner.registry

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.reader.value.expression.*
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

internal class ClassTestStepsRegistry(packages: Collection<String>) : TestStepRegistry {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ClassTestStepsRegistry::class.java)

        private val ID = AlphanumericValueExpression("id")
        private val TEST_STEP_NAME_PATTERN = AlphanumericValueExpression("testStepNamePattern")
        private val TEST_STEP_DEF_INSTANCE = AlphanumericValueExpression("testStepDefInstance")
        private val TEST_STEP_METHOD = AlphanumericValueExpression("testStepMethod")
    }

    private var testStepsPerClass: MutableCollection<Map<ValueExpression, ValueExpression?>> = mutableListOf()

    init {
        ClassGraph().acceptPackages(*packages.toTypedArray())
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use {
                it.allClasses
                    .forEach { c ->
                        loadStepDefsMethods(c)
                    }
            }
    }

    override fun getByName(testStepName: String): Map<ValueExpression, ValueExpression?>? =
        testStepsPerClass.firstOrNull { it[TEST_STEP_NAME_PATTERN]?.evaluate(ValueExpressionContext(testStepName)) as Boolean? == true }

    override fun getById(testStepId: String): Map<ValueExpression, ValueExpression?>? = getByName(testStepId)

    override fun getTestSteps(): Collection<Map<ValueExpression, ValueExpression?>> = testStepsPerClass

    private fun loadStepDefsMethods(classInfo: ClassInfo) {
        var foundClassInstance: Any? = null
        classInfo.methodInfo
            .filter { m -> m.hasAnnotation(TestStep::class.java) }
            .forEach { m ->
                LOGGER.debug("Extract test step from method in '${m.name}' of '${classInfo.name}'")

                foundClassInstance.let {
                    try {
                        foundClassInstance = classInfo.loadClass().getDeclaredConstructor().newInstance()
                    } catch (ex: NoSuchMethodException) {
                        throw TestStepRegistryException(
                            "Failed to instantiate class '${classInfo.name}'",
                            ex
                        )
                    }
                }
                val method = foundClassInstance?.let { i ->
                    i::class.java.methods.find { method -> method.name == m.name }
                }
                method?.let { methodInstance ->
                    val testStepAnnotation = methodInstance.getAnnotation(TestStep::class.java)
                    val testStepNameId = testStepAnnotation.id
                    val testStepNamePattern = PatternValueExpression(testStepAnnotation.name)

                    testStepsPerClass.add(
                        mapOf(
                            Pair(ID, StringValueExpression(testStepNameId)),
                            Pair(TEST_STEP_NAME_PATTERN, testStepNamePattern),
                            Pair(TEST_STEP_DEF_INSTANCE, AnyValueExpression(foundClassInstance!!)),
                            Pair(TEST_STEP_METHOD, AnyValueExpression(methodInstance))
                        )
                    )
                }
            }
    }
}
