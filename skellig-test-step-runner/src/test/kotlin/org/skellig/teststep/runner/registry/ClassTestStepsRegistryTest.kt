package org.skellig.teststep.runner.registry

import org.mockito.kotlin.mock
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skellig.teststep.runner.exception.TestStepRegistryException
import org.skellig.teststep.reader.value.expression.AlphanumericValueExpression
import org.skellig.teststep.reader.value.expression.AnyValueExpression
import org.skellig.teststep.reader.value.expression.PatternValueExpression
import org.skellig.teststep.runner.annotation.TestStep
import org.skellig.teststep.runner.fixture.lazysteps.LazySteps

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
            { assertEquals("step1", firstStep[AlphanumericValueExpression("id")].toString()) },
            { assertEquals("test A", (firstStep[AlphanumericValueExpression("testStepNamePattern")] as PatternValueExpression).pattern.pattern()) },
            { assertEquals(Steps::class.java, (firstStep[AlphanumericValueExpression("testStepDefClass")] as AnyValueExpression).evaluate(mock())) },
            { assertEquals(Steps::class.java.methods[0], (firstStep[AlphanumericValueExpression("testStepMethod")] as AnyValueExpression).evaluate(mock())) }
        )
    }

    @Test
    fun testExtractAnnotatedOverloadOnly() {
        val registry = ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.fixture.overload"))

        val testSteps = registry.getTestSteps()
        val method = (testSteps.single()[AlphanumericValueExpression("testStepMethod")] as AnyValueExpression).evaluate(mock()) as java.lang.reflect.Method
        val stepClass = (testSteps.single()[AlphanumericValueExpression("testStepDefClass")] as AnyValueExpression).evaluate(mock()) as Class<*>
        assertEquals("annotated:ab", method.invoke(stepClass.getDeclaredConstructor().newInstance(), "a", "b"))
    }

    @Test
    fun testRegisterMethodsWithInjectedParameters() {
        val registry = ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.fixture.injection"))

        assertEquals(5, registry.getTestSteps().size)
    }

    @Test
    fun testStepDefClassesAreNotInstantiatedAtStartup() {
        LazySteps.created.set(0)

        val registry = ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.fixture.lazysteps"))

        assertAll(
            { assertEquals(1, registry.getTestSteps().size) },
            { assertEquals(0, LazySteps.created.get()) },
        )
    }

    @Test
    fun testFailWhenClassHasNoConstructorWithoutArguments() {
        val ex = assertThrows<TestStepRegistryException> {
            ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.fixture.noargs"))
        }

        assertEquals("Failed to instantiate class 'org.skellig.teststep.runner.fixture.noargs.StepsWithoutNoArgConstructor'", ex.message)
    }

    @Test
    fun testFailWhenParametersDoNotMatchRegexGroups() {
        val ex = assertThrows<TestStepRegistryException> {
            ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.fixture.mismatch"))
        }

        assertEquals(
            "Test step method 'org.skellig.teststep.runner.fixture.mismatch.MismatchedSteps.tooFewParameters' has 1 parameter(s)" +
                    " which are not injected with the scenario state or scope, but the name 'needs (.+) and (.+)' has 2 regex group(s)." +
                    " Each regex group needs a parameter, in order, optionally followed by a Map of the test step parameters", ex.message
        )
    }

    @Test
    fun testParameterOfTypeAnyIsNotInjectedSoItCountsAsRegexGroup() {
        val ex = assertThrows<TestStepRegistryException> {
            ClassTestStepsRegistry(listOf("org.skellig.teststep.runner.fixture.mismatchmap"))
        }

        assertTrue(ex.message!!.contains("has 2 parameter(s)"), ex.message)
        assertTrue(ex.message!!.contains("has 0 regex group(s)"), ex.message)
    }
}

class Steps {

    @TestStep(id = "step1", name = "test A")
    fun step() {

    }

}