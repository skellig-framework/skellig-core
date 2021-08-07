package org.skellig.teststep.runner

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.util.CachedPattern.Companion.compile
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors

internal class TestStepsRegistry(private val testStepFileExtension: TestStepFileExtension,
                                 private val testStepReader: TestStepReader) : TestStepRegistry {

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
        val readFileStrategy = RawTestStepsReaderStrategy()
        return rootPaths
            .map { readFileStrategy.getTestStepsFromUri(it) }
            .flatten()
            .toList()
    }

    private inner class RawTestStepsReaderStrategy : RawTestStepsReader {
        private val fileReader = RawTestStepsFromFileReader()
        private val jarReader = RawTestStepsFromJarFileReader()

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> =
            try {
                if (rootUri.toURL().protocol == "jar") jarReader.getTestStepsFromUri(rootUri)
                else fileReader.getTestStepsFromUri(rootUri)
            } catch (ex: Exception) {
                throw TestStepRegistryException(ex.message, ex)
            }
    }

    private interface RawTestStepsReader {
        fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>>
    }

    private open inner class RawTestStepsFromFileReader : RawTestStepsReader {

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>> =
            walkThroughFiles(Paths.get(rootUri))

        protected fun walkThroughFiles(root: Path): Collection<Map<String, Any?>> =
            Files.walk(root)
                .parallel()
                .filter { it.toString().endsWith(testStepFileExtension.extension) }
                .map { testStepReader.read(it.toUri().toURL().openStream()) }
                .flatMap { it.stream() }
                .collect(Collectors.toList())
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