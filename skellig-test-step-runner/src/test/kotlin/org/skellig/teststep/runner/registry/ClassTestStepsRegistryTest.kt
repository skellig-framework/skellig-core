package org.skellig.teststep.runner.registry

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skellig.teststep.runner.annotation.TestStep

class ClassTestStepsRegistryTest {


    @Test
    fun testExtractTestStepsNonExistingPath() {
        val registry = ClassTestStepsRegistry(listOf("invalid.package"))

        assertEquals(0, registry.getTestSteps().size)
    }

    @Test
    fun testExtractTestStepsFromClasses() {
        val registry = ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.registry"))

        val testSteps = registry.getTestSteps()
        val firstStep = testSteps.first() as Map<*,*>
        assertAll(
            { assertEquals(1, testSteps.size) },
            { assertEquals(4, testSteps.first().size) },
            { assertEquals("step1", firstStep["id"].toString()) },
            { assertEquals("test A", firstStep["testStepNamePattern"].toString()) },
            { assertEquals(Steps::class.java, firstStep["testStepDefInstance"]?.javaClass) },
            { assertEquals(Steps::class.java.methods[0], firstStep["testStepMethod"]) }
        )
    }
}

class Steps {

    @TestStep(id = "step1", name = "test A")
    fun step() {

    }

}