package org.skellig.teststep.runner.registry

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.state.ScopeParameters
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.reader.value.expression.*
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.exception.TestStepRegistryException
import java.lang.reflect.Method
import java.util.regex.Pattern

/**
 * This class is an implementation of the TestStepRegistry interface.
 * It is responsible for scanning classes in the given packages and loading test step methods marked with the [@TestStep][TestStep] annotation.
 * The loaded test steps are stored in the testStepsPerClass collection and can be retrieved using the getByName, getById, and getTestSteps methods.
 *
 * @property packagesToScan the packages to scan for test step methods
 * @property classInstanceRegistry the registry for storing instances of test step classes
 */
internal class ClassTestStepsRegistry(
    packagesToScan: Collection<String>,
    private val classInstanceRegistry: MutableMap<Class<*>, Any>
) : TestStepRegistry {

    companion object {
        private val ID = AlphanumericValueExpression("id")
        private val TEST_STEP_NAME_PATTERN = AlphanumericValueExpression("testStepNamePattern")
        private val TEST_STEP_DEF_INSTANCE = AlphanumericValueExpression("testStepDefInstance")
        private val TEST_STEP_METHOD = AlphanumericValueExpression("testStepMethod")
    }

    private val log = logger<ClassTestStepsRegistry>()
    private var testStepsPerClass: MutableCollection<Map<ValueExpression, ValueExpression?>> = mutableListOf()

    init {
        log.debug {"Start to scan Test Step methods marked with ${TestStep::class.java.simpleName} in classes from packages $packagesToScan" }
        ClassGraph().acceptPackages(*packagesToScan.toTypedArray())
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
        classInfo.methodInfo
            .filter { m -> m.hasAnnotation(TestStep::class.java) }
            .forEach { m ->
                log.debug { "Extract test step from method in '${m.name}' of '${classInfo.name}'" }

                val instance = classInstanceRegistry.computeIfAbsent(classInfo.loadClass()) { type ->
                    try {
                        type.getDeclaredConstructor().newInstance()
                    } catch (ex: NoSuchMethodException) {
                        throw TestStepRegistryException("Failed to instantiate class '${type.name}'", ex)
                    }
                }

                val methodInstance = m.loadClassAndGetMethod()
                val testStepAnnotation = methodInstance.getAnnotation(TestStep::class.java)
                val testStepNameId = testStepAnnotation.id
                val testStepNamePattern = PatternValueExpression(testStepAnnotation.name)
                validateParameters(methodInstance, testStepAnnotation.name, testStepNamePattern.pattern)

                testStepsPerClass.add(
                    mapOf(
                        Pair(ID, StringValueExpression(testStepNameId)),
                        Pair(TEST_STEP_NAME_PATTERN, testStepNamePattern),
                        Pair(TEST_STEP_DEF_INSTANCE, AnyValueExpression(instance)),
                        Pair(TEST_STEP_METHOD, AnyValueExpression(methodInstance))
                    )
                )
            }
    }

    /**
     * The parameters which are not injected with the scenario state or scope must be the regex groups of the test step name
     * in order, optionally followed by a Map for the parameters of the test step.
     */
    private fun validateParameters(method: Method, testStepName: String, namePattern: Pattern) {
        val regexGroupCount = namePattern.matcher("").groupCount()
        val parameterTypes = method.parameterTypes.filter { !ScopeParameters.isInjected(it) }
        val takesMap = parameterTypes.isNotEmpty() && parameterTypes.last().isAssignableFrom(Map::class.java)
        if (parameterTypes.size != regexGroupCount && !(takesMap && parameterTypes.size - 1 == regexGroupCount)) {
            throw TestStepRegistryException(
                "Test step method '${method.declaringClass.name}.${method.name}' has ${parameterTypes.size} parameter(s)" +
                        " which are not injected with the scenario state or scope, but the name '$testStepName'" +
                        " has $regexGroupCount regex group(s)." +
                        " Each regex group needs a parameter, in order, optionally followed by a Map of the test step parameters"
            )
        }
    }
}
