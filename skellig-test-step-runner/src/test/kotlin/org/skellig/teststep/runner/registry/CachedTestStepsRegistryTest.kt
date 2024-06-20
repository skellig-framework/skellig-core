package org.skellig.teststep.runner.registry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.skellig.teststep.processing.model.factory.TestStepRegistry
import org.skellig.teststep.reader.value.expression.ValueExpression

class CachedTestStepsRegistryTest {

    private lateinit var cachedTestStepsRegistry: CachedTestStepsRegistry
    private lateinit var mockTestStepRegistry1: TestStepRegistry
    private lateinit var mockTestStepRegistry2: TestStepRegistry

    @BeforeEach
    fun setUp() {
        mockTestStepRegistry1 = mock()
        mockTestStepRegistry2 = mock()

        val registries = listOf(mockTestStepRegistry1, mockTestStepRegistry2)
        cachedTestStepsRegistry = CachedTestStepsRegistry(registries)

        whenever(mockTestStepRegistry1.getByName("unknown")).thenReturn(null)
        whenever(mockTestStepRegistry2.getByName("unknown")).thenReturn(null)
        whenever(mockTestStepRegistry1.getById("unknown")).thenReturn(null)
        whenever(mockTestStepRegistry2.getById("unknown")).thenReturn(null)
    }

    @Test
    fun `should return test step from cache when getByName is called`() {
        val mockTestStep: Map<ValueExpression, ValueExpression?> = mapOf(mock(), mock())
        whenever(mockTestStepRegistry1.getByName("testStep1")).thenReturn(mockTestStep)

        assertSame(mockTestStep, cachedTestStepsRegistry.getByName("testStep1"))
        verify(mockTestStepRegistry1).getByName("testStep1")
        verify(mockTestStepRegistry2, never()).getByName("testStep1")
        reset(mockTestStepRegistry1)

        // Call again to verify it's retrieved from cache
        assertSame(mockTestStep, cachedTestStepsRegistry.getByName("testStep1"))
        verifyNoInteractions(mockTestStepRegistry1, mockTestStepRegistry2)
    }

    @Test
    fun `should return null when getByName is called with an unknown name`() {
        assertNull(cachedTestStepsRegistry.getByName("unknown"))
        verify(mockTestStepRegistry1).getByName("unknown")
        verify(mockTestStepRegistry2).getByName("unknown")
    }

    @Test
    fun `should return test step when getById is called`() {
        val mockTestStep: Map<ValueExpression, ValueExpression?> = mapOf(mock(), mock())
        whenever(mockTestStepRegistry1.getById("id1")).thenReturn(null)
        whenever(mockTestStepRegistry2.getById("id1")).thenReturn(mockTestStep)

        assertSame(mockTestStep, cachedTestStepsRegistry.getById("id1"))
        verify(mockTestStepRegistry1).getById("id1")
        verify(mockTestStepRegistry2).getById("id1")
    }

    @Test
    fun `should return first found non-null test step when getById is called and all registries found test steps for same id`() {
        val mockTestStep: Map<ValueExpression, ValueExpression?> = mapOf(mock(), mock())
        whenever(mockTestStepRegistry1.getById("id1")).thenReturn(emptyMap())
        whenever(mockTestStepRegistry2.getById("id1")).thenReturn(mockTestStep)

        assertSame(mockTestStepRegistry1.getById("id1"), cachedTestStepsRegistry.getById("id1"))
    }

    @Test
    fun `should return null when getById is called with an unknown id`() {
        assertNull(cachedTestStepsRegistry.getById("unknown"))
        verify(mockTestStepRegistry1).getById("unknown")
        verify(mockTestStepRegistry2).getById("unknown")
    }

    @Test
    fun `should return all test steps when getTestSteps is called`() {
        val mockTestSteps1: Collection<Map<ValueExpression, ValueExpression?>> = listOf(mapOf(mock(), mock()))
        val mockTestSteps2: Collection<Map<ValueExpression, ValueExpression?>> = listOf(mapOf(mock(), mock()))
        whenever(mockTestStepRegistry1.getTestSteps()).thenReturn(mockTestSteps1)
        whenever(mockTestStepRegistry2.getTestSteps()).thenReturn(mockTestSteps2)

        val expectedTestSteps = listOf(mockTestSteps1, mockTestSteps2).flatten()

        assertEquals(expectedTestSteps, cachedTestStepsRegistry.getTestSteps())
        verify(mockTestStepRegistry1).getTestSteps()
        verify(mockTestStepRegistry2).getTestSteps()
    }
}