package org.skellig.teststep.runner.registry

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

internal class ClassTestStepsRegistry(packages: Collection<String>) : TestStepRegistry {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ClassTestStepsRegistry.javaClass)

        private const val ID = "id"
        private const val TEST_STEP_NAME_PATTERN = "testStepNamePattern"
        private const val TEST_STEP_DEF_INSTANCE = "testStepDefInstance"
        private const val TEST_STEP_METHOD = "testStepMethod"
    }

    private var testStepsPerClass: MutableCollection<Map<String, Any?>> = mutableListOf()

    init {
        ClassGraph().acceptPackages(*packages.toTypedArray())
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use {
                it.allClasses
                    .forEach { c ->
                        loadStepDefsMethods(c, it)
                    }
            }
    }

    override fun getByName(testStepName: String): Map<String, Any?>? =
        testStepsPerClass.firstOrNull { (it[TEST_STEP_NAME_PATTERN] as Pattern).matcher(testStepName).matches() }

    override fun getById(testStepId: String): Map<String, Any?>? = getByName(testStepId)

    override fun getTestSteps(): Collection<Map<String, Any?>> = testStepsPerClass

    private fun loadStepDefsMethods(classInfo: ClassInfo, it: ScanResult?) {
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
                    val testStepNamePattern = Pattern.compile(testStepAnnotation.name)

                    testStepsPerClass.add(
                        mapOf(
                            Pair(ID, testStepNameId),
                            Pair(TEST_STEP_NAME_PATTERN, testStepNamePattern),
                            Pair(TEST_STEP_DEF_INSTANCE, foundClassInstance!!),
                            Pair(TEST_STEP_METHOD, methodInstance)
                        )
                    )
                }
            }
    }
}
