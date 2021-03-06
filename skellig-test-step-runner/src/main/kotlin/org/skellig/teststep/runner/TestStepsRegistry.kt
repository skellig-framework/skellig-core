package org.skellig.teststep.runner

import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.processing.util.CachedPattern.Companion.compile
import org.skellig.teststep.reader.TestStepReader
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.runner.model.TestStepFileExtension
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

internal class TestStepsRegistry(private val testStepFileExtension: TestStepFileExtension,
                                 private val testStepReader: TestStepReader?) : TestStepRegistry {

    private var testSteps: Collection<Map<String, Any?>> = emptyList()
    private var testStepsRootPath: Collection<Path>? = null

    fun registerFoundTestStepsInPath(testStepsPaths: Collection<Path>) {
        testStepsRootPath = testStepsPaths
        testSteps = getTestStepsFromPath(testStepsPaths)
    }

    override fun getByName(testStepName: String): Map<String, Any?>? =
            testSteps.parallelStream()
                    .filter { testStep: Map<String, Any?> -> compile(getTestStepName(testStep)).matcher(testStepName).matches() }
                    .findFirst()
                    .orElse(null)

    private fun getTestStepName(rawTestStep: Map<String, Any?>): String =
            rawTestStep["name"]?.toString() ?: error("Attribute 'name' was not found in a raw Test Step $rawTestStep")

    private fun getTestStepsFromPath(rootPaths: Collection<Path>): Collection<Map<String, Any?>> {
        return rootPaths
                .map {
                    try {
                        return@map Files.walk(it)
                                .parallel()
                                .filter { path: Path -> path.fileName.toString().endsWith(testStepFileExtension.extension) }
                                .map { filePath: Path -> testStepReader!!.read(filePath) }
                                .flatMap { obj: List<Map<String, Any?>> -> obj.stream() }
                                .collect(Collectors.toList())
                    } catch (e: IOException) {
                        throw TestStepRegistryException(e.message, e)
                    }
                }
                .flatten()
                .toList()
    }
}