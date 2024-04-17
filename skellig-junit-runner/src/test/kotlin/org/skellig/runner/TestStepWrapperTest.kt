package org.skellig.runner

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.skellig.feature.TestStep

class TestStepWrapperTest {

    @Test
    fun testGetEntityName() {
        val testStep = mock<TestStep>()
        whenever(testStep.getEntityName()).thenReturn("name A")

        assertEquals(testStep.getEntityName(), TestStepWrapper(testStep).getEntityName())
    }
}