package org.skellig.teststep.processing.value.function

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.skellig.teststep.processing.state.ScopeParameters
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.value.exception.FunctionExecutionException
import org.skellig.teststep.processing.value.exception.FunctionRegistryException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * A class which registers custom functions as methods marked with @[Function] annotations in classes, and
 * can execute any of these custom functions.
 *
 * When instantiated, it scans for classes in [packages] which has methods marked with @[Function] and register them internally.
 *
 * Throws [FunctionExecutionException] if function is not found in the registry when calling [CustomFunctionExecutor.execute].
 *
 * @param packages The packages to scan for custom functions with @[Function]
 * @param classInstanceRegistry The registry of class instances. This registry keeps instances of classes instantiated earlier, so they can be reused elsewhere.
 * An example of where such classes can be instantiated are Test Step definition classes which may have test step
 * methods as well as methods marked as @[Function]. If not provided, then a new [HashMap] is assigned.
 */
class CustomFunctionExecutor(
    packages: Collection<String>?,
    private val classInstanceRegistry: MutableMap<Class<*>, Any> = mutableMapOf()
) : FunctionValueExecutor {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CustomFunctionExecutor::class.java)
    }

    private var functions: MutableMap<String, CustomFunction> = mutableMapOf()

    init {
        packages?.let {
            ClassGraph().acceptPackages(*packages.toTypedArray())
                .enableMethodInfo()
                .enableAnnotationInfo()
                .scan()
                .use {
                    it.allClasses
                        .forEach { c ->
                            loadCustomFunctions(c)
                        }
                }
        }
    }

    override fun execute(name: String, value: Any?, args: Array<Any?>): Any? = execute(name, value, args, null)

    override fun execute(name: String, value: Any?, args: Array<Any?>, scope: TestScenarioScope?): Any? {
        val function = functions[name] ?: throw FunctionExecutionException("Function '$name' was not found")
        return function.method.invoke(function.instance, *createMethodArguments(name, function.method, args, scope))
    }

    /**
     * Parameters of the method which are injected (see [ScopeParameters]) receive the state or the scope.
     * All other parameters receive the call arguments in order, so the injected ones never count as call arguments.
     */
    private fun createMethodArguments(name: String, method: Method, args: Array<Any?>, scope: TestScenarioScope?): Array<Any?> {
        if (method.parameterTypes.none { ScopeParameters.isInjected(it) }) return args

        val remainingArgs = args.iterator()
        return method.parameterTypes.map { type ->
            if (ScopeParameters.isInjected(type)) {
                val currentScope = scope ?: throw FunctionExecutionException(
                    "Function '$name' requires the scope of a test scenario but it is called outside of one"
                )
                ScopeParameters.valueFor(type, currentScope)
            } else if (remainingArgs.hasNext()) remainingArgs.next()
            else throw FunctionExecutionException("Function '$name' was called with ${args.size} arguments, which is not enough")
        }.toTypedArray()
    }

    override fun getFunctionName(): String = ""

    private fun loadCustomFunctions(classInfo: ClassInfo) {
        classInfo.methodInfo
            .filter { m -> m.hasAnnotation(Function::class.java) }
            .forEach { m ->
                LOGGER.debug("Extract test step from method in '${m.name}' of '${classInfo.name}'")

                val foundClassInstance = classInstanceRegistry.computeIfAbsent(classInfo.loadClass()) { type ->
                    try {
                        type.getDeclaredConstructor().newInstance()
                    } catch (ex: Exception) {
                        throw FunctionRegistryException("Failed to instantiate class '${type.name}'", ex)
                    }
                }
                val method = m.loadClassAndGetMethod()
                functions[method.name]?.let { existing ->
                    throw FunctionRegistryException(
                        "Function '${method.name}' is defined more than once: " +
                                "'${existing.method.declaringClass.name}.${existing.method.name}' and " +
                                "'${method.declaringClass.name}.${method.name}'"
                    )
                }
                functions[method.name] = CustomFunction(foundClassInstance, method)
            }
    }

    private data class CustomFunction(val instance: Any, val method: Method)
}

/**
 * Annotation used to mark a method of a class as a Skellig Function and registered in [CustomFunctionExecutor].
 * The name of the function is the same as the method name, and it must be unique across all scanned classes:
 * only the annotated method is called, even if other overloads exist, and two annotated methods with the same name
 * fail registration with [FunctionRegistryException].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Function