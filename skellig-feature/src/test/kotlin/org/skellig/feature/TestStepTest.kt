package org.skellig.feature

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("test equals and hashcode")
class TestStepTest {

    @Test
    fun `when new instances created with different parent`() {
        assertNotEquals(
            TestStep.Builder().withName("A").withParent("parentA").build(),
            TestStep.Builder().withName("A").withParent("parentB").build()
        )
        assertNotEquals(
            TestStep.Builder().withName("A").withParent("parentA").build().hashCode(),
            TestStep.Builder().withName("A").withParent("parentB").build().hashCode()
        )
    }

    @Test
    fun `when new instances created with same parent`() {
        assertEquals(
            TestStep.Builder().withName("A").withParent("parentA").build().hashCode(),
            TestStep.Builder().withName("A").withParent("parentA").build().hashCode()
        )
        assertEquals(
            TestStep.Builder().withName("A").withParent("parentA").build(),
            TestStep.Builder().withName("A").withParent("parentA").build()
        )
    }
}