package org.skellig.teststep.runner.registry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.registry.ClassTestStepsRegistry

class ClassTestStepsRegistryTest {


    @Test
    fun testExtractTestStepsNonExistingPath() {
        val registry = ClassTestStepsRegistry(listOf("invalid.package"), javaClass.classLoader)

        assertEquals(0, registry.getTestSteps().size)
    }

    @Test
    fun testExtractTestStepsFromClasses() {
        val registry = ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.registry"), javaClass.classLoader)

        val testSteps = registry.getTestSteps()
        assertAll(
            { assertEquals(1, testSteps.size) },
            { assertEquals(3, testSteps.first().size) },
            { assertEquals("test A", testSteps.first()["testStepNamePattern"].toString()) },
            { assertEquals(Steps::class.java, testSteps.first()["testStepDefInstance"]?.javaClass) },
            { assertEquals(Steps::class.java.methods[0], testSteps.first()["testStepMethod"]) }
        )
    }

}

class Steps {

    @TestStep("test A")
    fun step() {

    }

}