package org.skellig.teststep.processing.model.factory

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.skellig.teststep.processing.model.ClassTestStep
import org.skellig.teststep.processing.value.ValueExpressionContextFactory
import org.skellig.teststep.reader.value.expression.ValueExpression

class CompositeTestStepFactoryTest {

    private val registry = mock<TestStepRegistry>()
    private var contextFactory = mock<ValueExpressionContextFactory>()
    private var compositeFactory= CompositeTestStepFactory.Builder()
        .withTestDataRegistry(registry)
        .withValueExpressionContextFactory(contextFactory)
        .build()

    @Test
    fun `should register Test Step factory and create a relevant Test Step`() {
        val rawTestStep = mock<Map<ValueExpression, ValueExpression>>()
        val testStep = mock<ClassTestStep>()
        val testFactory = mock<ClassTestStepFactory>()
        whenever(testFactory.isConstructableFrom(rawTestStep)).thenReturn(true)
        whenever(testFactory.create("testStep", rawTestStep, emptyMap())).thenReturn(testStep)
        compositeFactory.registerTestStepFactory(testFactory)

        val createdStep = compositeFactory.create("testStep", rawTestStep, emptyMap())

        assertEquals(testStep, createdStep)
    }

    @Test
    fun `should always return true for isConstructableFrom`() {
        assertTrue(compositeFactory.isConstructableFrom(mapOf()))
    }
}