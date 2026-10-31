package org.skellig.teststep.runner.registry

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.state.ParallelGroupContext
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
 * The registry keeps the class and the method of every test step, not an instance: no instance is created here, and a
 * test step method is called on the instance of its class which belongs to the parallel group of the scope the test step
 * runs in. Classes without a public constructor without arguments fail registration, so the failure is not postponed
 * until the class is first used.
 *
 * @property packagesToScan the packages to scan for test step methods
 */
internal class ClassTestStepsRegistry(
    packagesToScan: Collection<String>
) : TestStepRegistry {

    companion object {
        private val ID = AlphanumericValueExpression("id")
        private val TEST_STEP_NAME_PATTERN = AlphanumericValueExpression("testStepNamePattern")
        private val TEST_STEP_DEF_CLASS = AlphanumericValueExpression("testStepDefClass")
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

                val stepDefClass = classInfo.loadClass()
                try {
                    ParallelGroupContext.requireNoArgConstructor(stepDefClass)
                } catch (ex: NoSuchMethodException) {
                    throw TestStepRegistryException("Failed to instantiate class '${stepDefClass.name}'", ex)
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
                        Pair(TEST_STEP_DEF_CLASS, AnyValueExpression(stepDefClass)),
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
