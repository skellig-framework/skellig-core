package org.skellig.teststep.processing.value.function

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.skellig.teststep.processing.state.ParallelGroupContext
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
 * The executor keeps the classes and methods of the functions, not instances. A function is called on the instance of its
 * class which belongs to the [parallel group][TestScenarioScope.parallelGroup] of the scope the function is called in,
 * so a function and a test step method of the same class share one instance within a parallel group.
 * A class without a public constructor without arguments fails registration, so the failure is not postponed
 * until the class is first used.
 *
 * @param packages The packages to scan for custom functions with @[Function]
 * @param defaultParallelGroup The parallel group whose instances are used when a function is called without a scope.
 * If not provided, then the executor has a parallel group of its own.
 */
class CustomFunctionExecutor(
    packages: Collection<String>?,
    private val defaultParallelGroup: ParallelGroupContext = ParallelGroupContext(ParallelGroupContext.DEFAULT_KEY)
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
        val instance = (scope?.parallelGroup ?: defaultParallelGroup).instanceOf(function.functionClass)
        return function.method.invoke(instance, *createMethodArguments(name, function.method, args, scope))
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

                val functionClass = classInfo.loadClass()
                try {
                    ParallelGroupContext.requireNoArgConstructor(functionClass)
                } catch (ex: NoSuchMethodException) {
                    throw FunctionRegistryException("Failed to instantiate class '${functionClass.name}'", ex)
                }
                val method = m.loadClassAndGetMethod()
                functions[method.name]?.let { existing ->
                    throw FunctionRegistryException(
                        "Function '${method.name}' is defined more than once: " +
                                "'${existing.method.declaringClass.name}.${existing.method.name}' and " +
                                "'${method.declaringClass.name}.${method.name}'"
                    )
                }
                functions[method.name] = CustomFunction(functionClass, method)
            }
    }

    private data class CustomFunction(val functionClass: Class<*>, val method: Method)
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