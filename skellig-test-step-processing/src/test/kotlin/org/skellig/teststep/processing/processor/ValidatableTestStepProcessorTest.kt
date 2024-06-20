package org.skellig.teststep.processing.processor

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.exception.ValidationException
import org.skellig.teststep.processing.model.DefaultTestStep
import org.skellig.teststep.processing.model.ValidationNode

class ValidatableTestStepProcessorTest {

    private val data = "data"
    private val testStep = mock<DefaultTestStep>()
    private val validationDetails = mock<ValidationNode>()

    @BeforeEach
    fun setUp() {
        whenever(testStep.validationDetails).thenReturn(validationDetails)
    }

    @Test
    fun `validate test step when it's valid`() {
        TestableValidatableStepProcessor().testValidate(testStep, data)
    }

    @Test
    fun `validate test step when it's not valid`() {
        doThrow(ValidationException::class).whenever(validationDetails).validate(data)

        assertThrows<ValidationException> { TestableValidatableStepProcessor().testValidate(testStep, data) }
    }

    @Test
    fun `check if test step valid when validation success`() {
        assertTrue(TestableValidatableStepProcessor().testIsValid(testStep, data))
    }

    @Test
    fun `check if test step valid when validation fails`() {
        doThrow(ValidationException::class).whenever(validationDetails).validate(data)

        assertFalse(TestableValidatableStepProcessor().testIsValid(testStep, data))
    }

    inner class TestableValidatableStepProcessor : ValidatableTestStepProcessor<DefaultTestStep>(mock()) {

        override fun process(testStep: DefaultTestStep): TestStepProcessor.TestStepRunResult {

            return BaseTestStepProcessor.DefaultTestStepRunResult(testStep)
        }

        fun testValidate(testStep: DefaultTestStep, actualResult: Any?) {
            super.validate(testStep, actualResult)
        }

        fun testIsValid(testStep: DefaultTestStep, actualResult: Any?): Boolean {
            return super.isValid(testStep, actualResult)
        }

        override fun getTestStepClass(): Class<*> = DefaultTestStep::class.java

    }
}