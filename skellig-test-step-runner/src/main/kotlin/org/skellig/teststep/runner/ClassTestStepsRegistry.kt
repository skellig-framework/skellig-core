package org.skellig.teststep.runner

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.exception.TestStepRegistryException
import java.io.File
import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.io.path.pathString

internal class ClassTestStepsRegistry(packages: Collection<String>, classLoader: ClassLoader) : TestStepRegistry {

    companion object {
        private const val CLASS_EXTENSION = ".class"
        private const val TEST_STEP_NAME_PATTERN = "testStepNamePattern"
        private const val TEST_STEP_DEF_INSTANCE = "testStepDefInstance"
        private const val TEST_STEP_METHOD = "testStepMethod"
    }

    private lateinit var testStepsPerClass: Collection<Map<String, Any?>>

    init {
        packages.forEach { resourcePath: String ->
            val packagePath = resourcePath.replace('.', '/')
            val resource = classLoader.getResource(packagePath)
            resource?.let {
                try {
                    testStepsPerClass = ClassTestStepsReaderStrategy(packagePath).getTestStepsFromUri(resource.toURI())
                } catch (e: Exception) {
                    throw TestStepRegistryException("Can't load the class", e)
                }
            }
        }
    }

    override fun getByName(testStepName: String): Map<String, Any?>? =
        testStepsPerClass.firstOrNull { (it[TEST_STEP_NAME_PATTERN] as Pattern).matcher(testStepName).matches() }


    private inner class ClassTestStepsReaderStrategy(packageName: String) : ClassTestStepsReader {
        private val fileReader = ClassTestStepsFromFileReader(packageName)
        private val jarReader = ClassTestStepsFromJarFileReader(packageName)

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> =
            try {
                if (rootUri.toURL().protocol == "jar") jarReader.getTestStepsFromUri(rootUri)
                else fileReader.getTestStepsFromUri(rootUri)
            } catch (ex: Exception) {
                throw TestStepRegistryException(ex.message, ex)
            }
    }

    private interface ClassTestStepsReader {
        fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>>
    }

    private open inner class ClassTestStepsFromFileReader(val packageName: String) : ClassTestStepsReader {

        private val classPackageName = packageName.replace('/', '.')

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> =
            walkThroughFiles(Paths.get(rootUri)) { it.toString().endsWith(CLASS_EXTENSION) }

        protected fun walkThroughFiles(root: Path, filter: (path: Path) -> Boolean): Collection<Map<String, Any?>> =
            Files.walk(root)
                .parallel()
                .filter { filter(it) }
                .map { readFromFile(it) }
                .flatMap { it.stream() }
                .collect(Collectors.toList())

        private fun readFromFile(file: Path): Collection<Map<String, Any?>> {
            val testStepsPerClass = mutableListOf<Map<String, Any?>>()
            val fileName = file.toString().substringAfter(packageName).replace('/', '.')
            val className = classPackageName + fileName.substringBeforeLast(CLASS_EXTENSION)
            val foundClass = Class.forName(className)
            var foundClassInstance: Any? = null

            foundClass.methods
                .filter { it.isAnnotationPresent(TestStep::class.java) }
                .forEach {
                    val testStepAnnotation = it.getAnnotation(TestStep::class.java)
                    val testStepNamePattern = Pattern.compile(testStepAnnotation.name)
                    foundClassInstance.let {
                        foundClassInstance = foundClass.newInstance()
                    }
                    testStepsPerClass.add(mapOf(
                        Pair(TEST_STEP_NAME_PATTERN, testStepNamePattern),
                        Pair(TEST_STEP_DEF_INSTANCE, foundClassInstance!!),
                        Pair(TEST_STEP_METHOD, it)))
                }
            return testStepsPerClass
        }
    }

    private inner class ClassTestStepsFromJarFileReader(packageName: String) :
        ClassTestStepsFromFileReader(packageName) {

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> {
            val testStepsPerClass = mutableListOf<Map<String, Any?>>()
            FileSystems.newFileSystem(rootUri, emptyMap<String, Any>()).use {
                val pathMatcher = it.getPathMatcher("regex:.*$packageName.*\\$CLASS_EXTENSION")
                for (root in it.rootDirectories) {
                    testStepsPerClass.addAll(walkThroughFiles(root) { path -> pathMatcher.matches(path) })
                }
            }
            return testStepsPerClass
        }
    }

}