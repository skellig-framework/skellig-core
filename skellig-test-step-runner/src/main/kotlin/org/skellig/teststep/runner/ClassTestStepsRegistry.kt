package org.skellig.teststep.runner

import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.exception.TestStepRegistryException
import java.io.File
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Pattern

internal class ClassTestStepsRegistry(packages: Collection<String>, classLoader: ClassLoader) {

    companion object {
        private const val CLASS_EXTENSION = ".class"
    }

    private val testStepsPerClass = mutableListOf<TestStepDefDetails>()

    init {
        packages.forEach { resourcePath: String ->
            val resource = classLoader.getResource(resourcePath.replace('.', '/'))
            resource?.let {
                try {
                    processDirectory(File(resource.path), resourcePath)
                } catch (e: Exception) {
                    throw TestStepRegistryException("Can't load the class", e)
                }
            }
        }
    }

    fun getTestStep(testStepName: String): TestStepDefDetails? {
        return testStepsPerClass
                .firstOrNull { it.testStepNamePattern.matcher(testStepName).matches() }
    }

    @Throws(Exception::class)
    private fun processDirectory(file: File, packageName: String) {
        for (fileName in Objects.requireNonNull(file.list())) {
            if (fileName.endsWith(CLASS_EXTENSION)) {
                val className = packageName + '.' + fileName.substring(0, fileName.length - CLASS_EXTENSION.length)
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
                            testStepsPerClass.add(TestStepDefDetails(testStepNamePattern, foundClassInstance!!, it))
                        }
            } else {
                val subDir = File(file, fileName)
                if (subDir.isDirectory) {
                    processDirectory(subDir, "$packageName.$fileName")
                }
            }
        }
    }

    class TestStepDefDetails(val testStepNamePattern: Pattern,
                             val testStepDefInstance: Any,
                             val testStepMethod: Method)
}