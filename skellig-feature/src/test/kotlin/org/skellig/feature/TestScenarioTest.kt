package org.skellig.feature

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("test equals and hashcode")
class TestScenarioTest {

    @Test
    fun `when new instances created with different parent`() {
        assertNotEquals(
            TestScenario.Builder().withName("A").withParent("parentA").build(),
            TestScenario.Builder().withName("A").withParent("parentB").build()
        )
        assertNotEquals(
            TestScenario.Builder().withName("A").withParent("parentA").build().hashCode(),
            TestScenario.Builder().withName("A").withParent("parentB").build().hashCode()
        )
    }

    @Test
    fun `when new instances created with same parent`() {
        assertEquals(
            TestScenario.Builder().withName("A").withParent("parentA").build().hashCode(),
            TestScenario.Builder().withName("A").withParent("parentA").build().hashCode()
        )
        assertEquals(
            TestScenario.Builder().withName("A").withParent("parentA").build(),
            TestScenario.Builder().withName("A").withParent("parentA").build()
        )
    }
}