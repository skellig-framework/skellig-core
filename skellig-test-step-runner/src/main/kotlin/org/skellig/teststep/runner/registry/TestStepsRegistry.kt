package org.skellig.teststep.runner.registry

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.util.CachedPattern.Companion.compile
import org.skellig.teststep.processing.util.debug
import org.skellig.teststep.processing.util.logger
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.reader.exception.TestStepReadException
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpression
import org.skellig.teststep.reader.value.expression.ValueExpressionContext
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

internal class TestStepsRegistry(
    private val testStepFileExtension: TestStepFileExtension,
    private val testStepReader: TestStepReader
) : TestStepRegistry {

    companion object {
        private val ID = AlphanumericValueExpression("id")
        private val NAME = AlphanumericValueExpression("name")
    }

    private val log = logger<TestStepsRegistry>()
    private var testSteps: Collection<Map<ValueExpression, ValueExpression?>> = emptyList()
    private var testStepsGroupedById: Map<String, Map<ValueExpression, ValueExpression?>> = mutableMapOf()

    fun registerFoundTestStepsInPath(testStepsPaths: Collection<URI>) {
        testSteps = getTestStepsFromPath(testStepsPaths)
        testStepsGroupedById = testSteps.filter { it.containsKey(ID) }.associateBy { it[ID].toString() }
    }

    override fun getByName(testStepName: String): Map<ValueExpression, ValueExpression?>? =
        testSteps.parallelStream()
            .filter { testStep: Map<ValueExpression, ValueExpression?> ->
                compile(getTestStepName(testStep)).matcher(testStepName).matches()
            }
            .findFirst()
            .orElse(null)

    override fun getById(testStepId: String): Map<ValueExpression, ValueExpression?>? =
        testStepsGroupedById[testStepId]

    override fun getTestSteps(): Collection<Map<ValueExpression, ValueExpression?>> = testSteps

    private fun getTestStepName(rawTestStep: Map<ValueExpression, ValueExpression?>): String =
        rawTestStep[NAME]?.evaluate(ValueExpressionContext.EMPTY)?.toString() ?: error("Attribute 'name' was not found in a raw Test Step $rawTestStep")

    private fun getTestStepsFromPath(rootPaths: Collection<URI>): Collection<Map<ValueExpression, ValueExpression?>> {
        log.debug { "Start to scan Test Steps in paths: '$rootPaths'" }

        val readFileStrategy = RawTestStepsReaderStrategy()
        return rootPaths
            .map { readFileStrategy.getTestStepsFromUri(it) }
            .flatten()
            .toList()
    }

    private inner class RawTestStepsReaderStrategy : RawTestStepsReader {
        private val fileReader = mapOf(
            Pair("file", RawTestStepsFromFileReader()),
            Pair("jar", RawTestStepsFromJarFileReader())
        )


        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<ValueExpression, ValueExpression?>> =
            try {
                val protocol = rootUri.toURL().protocol
                fileReader[protocol]?.getTestStepsFromUri(rootUri)
                    ?: error(
                        "File protocol '$protocol' is not supported " +
                                "when reading test steps from files on URI '$rootUri'"
                    )
            } catch (ex: Exception) {
                throw TestStepRegistryException("Failed to register test steps in '$rootUri'", ex)
            }
    }

    private open inner class RawTestStepsFromFileReader : RawTestStepsReader {

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<ValueExpression, ValueExpression?>> =
            walkThroughFiles(Paths.get(rootUri))

        protected fun walkThroughFiles(root: Path): Collection<Map<ValueExpression, ValueExpression?>> =
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

        private fun readFileFromPath(it: Path): List<Map<ValueExpression, ValueExpression?>> {
            log.debug { "Extract test steps from file '$it'" }
            try {
                return it.toUri().toURL().openStream().use { testStepReader.read(it) }
            } catch (ex: TestStepReadException) {
                throw TestStepReadException("Failed to read test steps in file '$it'", ex)
            }
        }
    }

    private inner class RawTestStepsFromJarFileReader : RawTestStepsFromFileReader() {

        override fun getTestStepsFromUri(rootUri: URI): Collection<Map<ValueExpression, ValueExpression?>> {
            val rawTestSteps = mutableListOf<Map<ValueExpression, ValueExpression?>>()
            FileSystems.newFileSystem(rootUri, emptyMap<String, Any>()).use {
                for (root in it.rootDirectories) {
                    rawTestSteps.addAll(walkThroughFiles(root))
                }
            }
            return rawTestSteps
        }
    }
}