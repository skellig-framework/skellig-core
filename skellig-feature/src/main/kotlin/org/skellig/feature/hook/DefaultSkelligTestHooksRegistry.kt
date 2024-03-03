package org.skellig.feature.hook

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.skellig.feature.exception.SkelligClassInstanceRegistryException
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DefaultSkelligTestHooksRegistry(
    packages: Collection<String>,
    private val classInstanceRegistry: MutableMap<Class<*>, Any>) : SkelligTestHooksRegistry {

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

                val instance = classInstanceRegistry.computeIfAbsent(classInfo.loadClass()) { type ->
                    try {
                        type.getDeclaredConstructor().newInstance()
                    } catch (ex: NoSuchMethodException) {
                        throw SkelligClassInstanceRegistryException("Failed to instantiate class '${type.name}'." +
                                " The hook class must have default constructor.", ex)
                    }
                }

                val method = instance::class.java.methods.find { method -> method.name == m.name }
                method?.let { methodInstance ->
                    val annotation = methodInstance.getAnnotation(hookType)
                    val tags = extractTagsFromHookAnnotation(annotation)
                    val order = extractOrderFromHookAnnotation(annotation)
                    hooks.add(SkelligHook(tags, methodInstance, instance, order, hookType))
                }
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
        return if(tags.isEmpty()) null else tags.toSet()
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