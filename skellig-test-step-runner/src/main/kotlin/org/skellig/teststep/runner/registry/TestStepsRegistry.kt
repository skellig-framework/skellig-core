package org.skellig.teststep.runner.registry

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.util.CachedPattern.Companion.compile
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.*
import java.util.stream.Collectors

internal class TestStepsRegistry(private val testStepFileExtension: TestStepFileExtension,
                                 private val testStepReader: TestStepReader) : TestStepRegistry {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(TestStepsRegistry.javaClass)
    }

    private var testSteps: Collection<Map<String, Any?>> = emptyList()

    fun registerFoundTestStepsInPath(testStepsPaths: Collection<URI>) {
        testSteps = getTestStepsFromPath(testStepsPaths)
    }

    override fun getByName(testStepName: String): Map<String, Any?>? =
        testSteps.parallelStream()
            .filter { testStep: Map<String, Any?> ->
                compile(getTestStepName(testStep)).matcher(testStepName).matches()
            }
            .findFirst()
            .orElse(null)

    override fun getTestSteps(): Collection<Map<String, Any?>> = testSteps

    private fun getTestStepName(rawTestStep: Map<String, Any?>): String =
        rawTestStep["name"]?.toString() ?: error("Attribute 'name' was not found in a raw Test Step $rawTestStep")

    private fun getTestStepsFromPath(rootPaths: Collection<URI>): Collection<Map<String, Any?>> {
        LOGGER.debug("Extracting test steps from files in '$rootPaths'")

        val readFileStrategy = RawTestStepsReaderStrategy()
        return rootPaths
            .map { readFileStrategy.getTestStepsFromUri(it) }
            .flatten()
            .toList()
    }

    private inner class RawTestStepsReaderStrategy : RawTestStepsReader {
        private val fileReader = mapOf(Pair("file", RawTestStepsFromFileReader()),
                                       Pair("jar", RawTestStepsFromJarFileReader()))


        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> =
            try {
                val protocol = rootUri.toURL().protocol
                fileReader[protocol]?.getTestStepsFromUri(rootUri)
                    ?: error("File protocol '$protocol' is not supported " +
                                     "when reading test steps from files on URI '$rootUri'")
            } catch (ex: Exception) {
                throw TestStepRegistryException(ex.message, ex)
            }
    }

    private open inner class RawTestStepsFromFileReader : RawTestStepsReader {

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> =
            walkThroughFiles(Paths.get(rootUri))

        protected fun walkThroughFiles(root: Path): Collection<Map<String, Any?>> =
            if (Files.isDirectory(root)) {
                Files.walk(root)
                    .parallel()
                    .filter { it.toString().endsWith(testStepFileExtension.extension) }
                    .map { readFileFromPath(it) }
                    .flatMap { it.stream() }
                    .collect(Collectors.toList())
            } else {
                readFileFromPath(root)
            }

        private fun readFileFromPath(it: Path): List<Map<String, Any?>> {
            LOGGER.debug("Extract test steps from file '$it'")
            return testStepReader.read(it.toUri().toURL().openStream())
        }
    }

    private inner class RawTestStepsFromJarFileReader : RawTestStepsFromFileReader() {

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> {
            val rawTestSteps = mutableListOf<Map<String, Any?>>()
            FileSystems.newFileSystem(rootUri, emptyMap<String, Any>()).use {
                for (root in it.rootDirectories) {
                    rawTestSteps.addAll(walkThroughFiles(root))
                }
            }
            return rawTestSteps
        }
    }
}