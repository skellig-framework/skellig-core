package org.skellig.teststep.runner.registry

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URI
import java.nio.file.*
import java.util.regex.Pattern
import java.util.stream.Collectors

internal class ClassTestStepsRegistry(packages: Collection<String>, classLoader: ClassLoader) : TestStepRegistry {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ClassTestStepsRegistry.javaClass)

        private const val CLASS_EXTENSION = ".class"
        private const val TEST_STEP_NAME_PATTERN = "testStepNamePattern"
        private const val TEST_STEP_DEF_INSTANCE = "testStepDefInstance"
        private const val TEST_STEP_METHOD = "testStepMethod"
    }

    private var testStepsPerClass: Collection<Map<String, Any?>> = emptyList()

    init {
        packages.forEach { resourcePath: String ->
            val packagePath = resourcePath.replace('.', File.separatorChar)
            val resource = classLoader.getResource(packagePath)
            resource?.let {
                LOGGER.debug("Extracting test steps from classes in '$packagePath'")
                try {
                    testStepsPerClass = ClassTestStepsReaderStrategy(packagePath).getTestStepsFromUri(resource.toURI())
                } catch (e: Exception) {
                    throw TestStepRegistryException("Can't extract test steps from classes in '$packagePath'", e)
                }
            }
        }
    }

    override fun getByName(testStepName: String): Map<String, Any?>? =
        testStepsPerClass.firstOrNull { (it[TEST_STEP_NAME_PATTERN] as Pattern).matcher(testStepName).matches() }

    override fun getById(testStepId: String): Map<String, Any?>? = getByName(testStepId)

    override fun getTestSteps(): Collection<Map<String, Any?>> = testStepsPerClass


    private inner class ClassTestStepsReaderStrategy(packageName: String) : RawTestStepsReader {
        private val fileReader = mapOf(
            Pair("file", ClassTestStepsFromFileReader(packageName)),
            Pair("jar", ClassTestStepsFromJarFileReader(packageName))
        )

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> =
            try {
                val protocol = rootUri.toURL().protocol
                fileReader[protocol]?.getTestStepsFromUri(rootUri)
                    ?: error(
                        "File protocol '$protocol' is not supported " +
                                "when reading test steps from classes on URI '$rootUri'"
                    )
            } catch (ex: Exception) {
                throw TestStepRegistryException(ex.message, ex)
            }
    }

    private open inner class ClassTestStepsFromFileReader(val packageName: String) : RawTestStepsReader {

        private val classPackageName = packageName.replace(File.separatorChar, '.')

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
            val fileName = file.toString().substringAfter(packageName).replace(File.separatorChar, '.')
            val className = classPackageName + fileName.substringBeforeLast(CLASS_EXTENSION)
            val foundClass = Class.forName(className)
            var foundClassInstance: Any? = null

            foundClass.methods
                .filter { it.isAnnotationPresent(TestStep::class.java) }
                .forEach {
                    LOGGER.debug("Extract test step from method in '${it.name}' of '$className'")

                    val testStepAnnotation = it.getAnnotation(TestStep::class.java)
                    val testStepNamePattern = Pattern.compile(testStepAnnotation.name)
                    foundClassInstance.let {
                        try {
                            foundClassInstance = foundClass.getDeclaredConstructor().newInstance()
                        } catch (ex: NoSuchMethodException) {
                            throw TestStepRegistryException("Failed to instantiate class '$className'", ex)
                        }
                    }
                    testStepsPerClass.add(
                        mapOf(
                            Pair(TEST_STEP_NAME_PATTERN, testStepNamePattern),
                            Pair(TEST_STEP_DEF_INSTANCE, foundClassInstance!!),
                            Pair(TEST_STEP_METHOD, it)
                        )
                    )
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