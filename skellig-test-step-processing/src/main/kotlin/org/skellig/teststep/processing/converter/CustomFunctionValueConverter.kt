package org.skellig.teststep.processing.converter

import org.skellig.teststep.processing.exception.TestValueConversionException
import org.skellig.teststep.processing.experiment.FunctionValueProcessor
import java.io.File
import java.lang.reflect.Method
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

class CustomFunctionValueConverter(packages: Collection<String>?, classLoader: ClassLoader?) : FunctionValueProcessor {

    companion object {
        private const val CLASS_EXTENSION = ".class"
    }

    private var functions: Map<String, CustomFunction> = emptyMap()

    init {
        packages?.forEach { resourcePath: String ->
            val packagePath = resourcePath.replace('.', File.separatorChar)
            val resource = classLoader?.getResource(packagePath)
            resource?.let {
                try {
                    functions = ClassTestStepsReaderStrategy(packagePath).getFromUri(resource.toURI())
                } catch (e: Exception) {
                    throw TestValueConversionException("Can't extract test steps from classes in '$packagePath'", e)
                }
            }
        }
    }

    override fun execute(name: String, args: Array<Any?>): Any? {
        return functions[name]?.let {
            return if (args.isNotEmpty()) it.method.invoke(it.instance, *args)
            else it.method.invoke(it.instance)
        }
    }

    override fun getFunctionName(): String = ""

    private inner class ClassTestStepsReaderStrategy(packageName: String) {
        private val fileReader = mapOf(
            Pair("file", ClassFunctionsFromFileReader(packageName)),
            Pair("jar", ClassFunctionsFromJarFileReader(packageName))
        )

        fun getFromUri(rootUri: URI): Map<String, CustomFunction> {
            val protocol = rootUri.toURL().protocol
            return fileReader[protocol]?.getFromUri(rootUri)
                ?: error(
                    "File protocol '$protocol' is not supported " +
                            "when reading test steps from classes on URI '$rootUri'"
                )
        }
    }

    private open inner class ClassFunctionsFromFileReader(val packageName: String) {

        private val classPackageName = packageName.replace(File.separatorChar, '.')

        open fun getFromUri(rootUri: URI): Map<String, CustomFunction> =
            walkThroughFiles(Paths.get(rootUri)) { it.toString().endsWith(CLASS_EXTENSION) }

        protected fun walkThroughFiles(root: Path, filter: (path: Path) -> Boolean): Map<String, CustomFunction> {
            val functions = mutableMapOf<String, CustomFunction>()
            Files.walk(root)
                .parallel()
                .filter { filter(it) }
                .map { readFromFile(it) }
                .forEach { functions.putAll(it) }
            return functions
        }

        private fun readFromFile(file: Path): Map<String, CustomFunction> {
            val testStepsPerClass = mutableMapOf<String, CustomFunction>()
            val fileName = file.toString().substringAfter(packageName).replace(File.separatorChar, '.')
            val className = classPackageName + fileName.substringBeforeLast(CLASS_EXTENSION)
            val foundClass = Class.forName(className)
            var foundClassInstance: Any? = null

            foundClass.methods
                .filter { it.isAnnotationPresent(Function::class.java) }
                .forEach {
                    foundClassInstance.let {
                        try {
                            foundClassInstance = foundClass.getDeclaredConstructor().newInstance()
                        } catch (ex: NoSuchMethodException) {
                            throw TestValueConversionException("Failed to instantiate class '$className'", ex)
                        }
                    }
                    testStepsPerClass[it.name] = CustomFunction(foundClassInstance!!, it)
                }
            return testStepsPerClass
        }
    }

    private inner class ClassFunctionsFromJarFileReader(packageName: String) :
        ClassFunctionsFromFileReader(packageName) {

        override fun getFromUri(rootUri: URI): Map<String, CustomFunction> {
            val testStepsPerClass = mutableMapOf<String, CustomFunction>()
            FileSystems.newFileSystem(rootUri, emptyMap<String, Any>()).use {
                val pathMatcher = it.getPathMatcher("regex:.*$packageName.*\\$CLASS_EXTENSION")
                for (root in it.rootDirectories) {
                    testStepsPerClass.putAll(walkThroughFiles(root) { path -> pathMatcher.matches(path) })
                }
            }
            return testStepsPerClass
        }
    }

    private data class CustomFunction(val instance: Any, val method: Method)

}

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class Function