package org.skellig.feature.hook

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.skellig.feature.exception.SkelligClassInstanceRegistryException
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.teststep.processing.state.ParallelGroupContext
import org.skellig.teststep.processing.state.ScopeParameters
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Default implementation of the [SkelligTestHooksRegistry] interface.
 *
 * This class is responsible for managing and retrieving [SkelligHook] objects.
 *
 * When instantiated, it scans for classes where methods are marked with the following annotations:
 * - [BeforeTestFeature]
 * - [BeforeTestScenario]
 * - [AfterTestFeature]
 * - [BeforeTestScenario]
 * and register them internally with their classes. No instances are created here: a hook runs on the instance of its class
 * which belongs to the parallel group of the scope it runs in, see [SkelligHook.run]. Classes without a public
 * constructor without arguments fail registration, so the failure is not postponed until the class is first used.
 *
 * @param packages The collection of packages to scan for hook annotations.
 */
class DefaultSkelligTestHooksRegistry(
    packages: Collection<String>
) : SkelligTestHooksRegistry {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(DefaultSkelligTestHooksRegistry::class.java)
    }

    private var hooks: MutableCollection<SkelligHook> = mutableListOf()

    init {
        ClassGraph().acceptPackages(*packages.toTypedArray())
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use {
                it.allClasses.forEach { c ->
                    listOf(
                        BeforeTestScenario::class.java,
                        AfterTestScenario::class.java,
                        BeforeTestFeature::class.java,
                        AfterTestFeature::class.java,
                    ).forEach { hookType -> loadStepDefsMethods(c, hookType) }
                }
            }
    }

    override fun getByTags(hookType: Class<*>, tags: Set<String>?): Collection<SkelligHook> {
        return hooks.filter { h ->
            h.type == hookType &&
                    tags?.let { (h.tags?.intersect(tags)?.isNotEmpty()) ?: true } ?: (h.tags == null)
        }
    }

    override fun getHooks(): Collection<SkelligHook> = hooks

    private fun loadStepDefsMethods(classInfo: ClassInfo, hookType: Class<out Annotation>) {
        classInfo.methodInfo
            .filter { m -> m.hasAnnotation(hookType) }
            .forEach { m ->
                LOGGER.debug("Extract @BeforeTestScenario hook from method in '${m.name}' of '${classInfo.name}'")

                val hookClass = classInfo.loadClass()
                try {
                    ParallelGroupContext.requireNoArgConstructor(hookClass)
                } catch (ex: NoSuchMethodException) {
                    throw SkelligClassInstanceRegistryException(
                        "Failed to instantiate class '${hookClass.name}'." +
                                " The hook class must have default constructor.", ex
                    )
                }

                val method = hookClass.methods.find { method -> method.name == m.name && method.isAnnotationPresent(hookType) }
                method?.let { methodInstance ->
                    validateParameters(methodInstance)
                    val annotation = methodInstance.getAnnotation(hookType)
                    val tags = extractTagsFromHookAnnotation(annotation)
                    val order = extractOrderFromHookAnnotation(annotation)
                    hooks.add(SkelligHook(tags, methodInstance, hookClass, order, hookType))
                }
            }
    }

    private fun validateParameters(method: Method) {
        method.parameterTypes.firstOrNull { !ScopeParameters.isInjected(it) }?.let {
            throw SkelligClassInstanceRegistryException(
                "Hook method '${method.declaringClass.name}.${method.name}' has a parameter of type '${it.name}'" +
                        " which cannot be injected." +
                        " Hooks can only declare parameters of type TestScenarioState or TestScenarioScope", null
            )
        }
    }

    private fun extractTagsFromHookAnnotation(annotation: Annotation): Set<String>? {
        val tags = when (annotation) {
            is BeforeTestScenario -> annotation.tags
            is AfterTestScenario -> annotation.tags
            is BeforeTestFeature -> annotation.tags
            is AfterTestFeature -> annotation.tags
            else -> throw IllegalArgumentException(
                "Unexpected hook annotation provided. " +
                        "Wanted: BeforeTestScenario, AfterTestScenario, BeforeAll or AfterAll], " +
                        "but was: ${annotation::class.java}"
            )
        }
        return if (tags.isEmpty()) null else tags.toSet()
    }

    private fun extractOrderFromHookAnnotation(annotation: Annotation): Int {
        return when (annotation) {
            is BeforeTestScenario -> annotation.order
            is AfterTestScenario -> annotation.order
            is BeforeTestFeature -> annotation.order
            is AfterTestFeature -> annotation.order
            else -> throw IllegalArgumentException(
                "Unexpected hook annotation provided. " +
                        "Wanted: BeforeTestScenario, AfterTestScenario, BeforeAll or AfterAll], " +
                        "but was: ${annotation::class.java}"
            )
        }
    }

}