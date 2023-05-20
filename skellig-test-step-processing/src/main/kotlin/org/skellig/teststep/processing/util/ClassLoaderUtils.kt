package org.skellig.teststep.processing.util

import java.io.File
import java.lang.RuntimeException
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

private const val CLASS_EXTENSION = ".class"

open class ClassReaderStrategy<T>(
    packageName: String,
    converter: (foundClass: Class<*>) -> Collection<T>
) {
    private val fileReader = mapOf(
        Pair("file", ClassFromFileReader(packageName, converter)),
        Pair("jar", ClassFromJarFileReader(packageName, converter))
    )

    fun getFromUriAndConvert(rootUri: URI): Collection<T> =
        try {
            val protocol = rootUri.toURL().protocol
            fileReader[protocol]?.getFromUriAndConvert(rootUri)
                ?: error(
                    "File protocol '$protocol' is not supported " +
                            "when reading test steps from classes on URI '$rootUri'"
                )
        } catch (ex: Exception) {
            throw ClassReaderException(ex.message, ex)
        }
}

open class ClassFromFileReader<T>(
    val packageName: String,
    val converter: (foundClass: Class<*>) -> Collection<T>
) {

    private val classPackageName = packageName.replace(File.separatorChar, '.')

    open fun getFromUriAndConvert(rootUri: URI): Collection<T> =
        walkThroughFiles(Paths.get(rootUri)) { it.toString().endsWith(CLASS_EXTENSION) }

    protected fun walkThroughFiles(
        root: Path,
        filter: (path: Path) -> Boolean
    ): Collection<T> =
        Files.walk(root)
            .parallel()
            .filter { filter(it) }
            .map { readFromFile(it) }
            .flatMap { it.stream() }
            .collect(Collectors.toList())

    private fun readFromFile(file: Path): Collection<T> {
        val fileName = file.toString().substringAfter(packageName).replace(File.separatorChar, '.')
        val className = classPackageName + fileName.substringBeforeLast(CLASS_EXTENSION)
        val foundClass = Class.forName(className)

        return convert(foundClass)
    }

    protected fun convert(foundClass: Class<*>): Collection<T> = converter.invoke(foundClass)
}

open class ClassFromJarFileReader<T>(
    packageName: String,
    converter: (foundClass: Class<*>) -> Collection<T>
) : ClassFromFileReader<T>(packageName, converter) {

    override fun getFromUriAndConvert(rootUri: URI): Collection<T> {
        val convertedValuesFromClass = mutableListOf<T>()
        FileSystems.newFileSystem(rootUri, emptyMap<String, Any>()).use {
            val pathMatcher = it.getPathMatcher("regex:.*$packageName.*\\$CLASS_EXTENSION")
            for (root in it.rootDirectories) {
                convertedValuesFromClass.addAll(walkThroughFiles(root) { path -> pathMatcher.matches(path) })
            }
        }
        return convertedValuesFromClass
    }
}

class ClassReaderException(message: String?, ex: Exception) : RuntimeException(message, ex)