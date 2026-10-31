package org.skellig.teststep.processing.state

import java.io.Closeable
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds the instances of the user classes (test step definitions, hooks and classes with functions) which one
 * parallel group uses. Every parallel group has its own context, so fields in user classes are never shared between
 * parallel groups, and they carry over between the scenarios of the same parallel group.
 *
 * The instance of a class is created on the first use, with the constructor without arguments, and then
 * [instanceInitializer] gets the new instance, which is how classes implementing `SkelligTestContextAware` receive the
 * Skellig context. The same instance is returned from then on, until the context is [closed][close].
 *
 * A step method, a hook method and a function of the same class share the one instance of the context.
 *
 * @property key Identifies the parallel group in logs and messages.
 * @property instanceInitializer Called once for every created instance, before it is used.
 */
class ParallelGroupContext(
    val key: String,
    private val instanceInitializer: (Any) -> Unit = {}
) : Closeable {

    private val instances = ConcurrentHashMap<Class<*>, Lazy<Any>>()

    /**
     * Returns the instance of the [type] of this parallel group, and creates it if this is the first use.
     * Concurrent calls for the same [type] get the same instance, which is created once.
     *
     * @throws IllegalStateException if the instance cannot be created. The next call tries again.
     */
    fun <T : Any> instanceOf(type: Class<T>): T {
        // creating the instance is not done in the compute function of the map, as the user code run there,
        // such as the constructor, can use this context for another class
        val instance = instances.computeIfAbsent(type) { lazy { create(type) } }.value
        return type.cast(instance)
    }

    private fun create(type: Class<*>): Any {
        val instance = try {
            requireNoArgConstructor(type).newInstance()
        } catch (ex: NoSuchMethodException) {
            throw IllegalStateException(
                "Failed to create an instance of class '${type.name}' in parallel group '$key'." +
                        " The class must have a public constructor without arguments", ex
            )
        } catch (ex: InvocationTargetException) {
            throw IllegalStateException("Failed to create an instance of class '${type.name}' in parallel group '$key'", ex.targetException)
        } catch (ex: ReflectiveOperationException) {
            throw IllegalStateException("Failed to create an instance of class '${type.name}' in parallel group '$key'", ex)
        }
        instanceInitializer(instance)
        return instance
    }

    /**
     * Drops all instances of this context, which is done when the parallel group ends.
     * If a class is used again, then it gets a new instance.
     */
    override fun close() {
        instances.clear()
    }

    override fun toString(): String = "parallelGroup($key)"

    companion object {

        /**
         * The key of the parallel group used when no parallel run is configured.
         */
        const val DEFAULT_KEY = "default"

        /**
         * Checks that instances of the [type] can be created by a [ParallelGroupContext], without creating one.
         * Registries use it to fail at startup instead of at the first use of a class.
         *
         * @return the constructor without arguments.
         * @throws NoSuchMethodException if the [type] is abstract, or has no public constructor without arguments.
         */
        fun requireNoArgConstructor(type: Class<*>): Constructor<*> {
            if (Modifier.isAbstract(type.modifiers)) {
                throw NoSuchMethodException("Class '${type.name}' is abstract, so it has no instances")
            }
            val constructor = type.getDeclaredConstructor()
            if (!Modifier.isPublic(constructor.modifiers)) {
                throw NoSuchMethodException("The constructor without arguments of class '${type.name}' is not public")
            }
            return constructor
        }
    }
}
