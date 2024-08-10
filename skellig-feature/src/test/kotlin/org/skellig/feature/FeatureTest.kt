package org.skellig.feature

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("test equals and hashcode")
class FeatureTest {

    @Test
    fun `when new instances created with different file path`() {
        assertNotEquals(
            Feature.Builder().withName("A").withFilePath("pathA").build(),
            Feature.Builder().withName("A").withFilePath("pathB").build()
        )
        assertNotEquals(
            Feature.Builder().withName("A").withFilePath("pathA").build().hashCode(),
            Feature.Builder().withName("A").withFilePath("pathB").build().hashCode()
        )
    }

    @Test
    fun `when new instances created with same file path`() {
        assertEquals(
            Feature.Builder().withName("A").withFilePath("pathA").build().hashCode(),
            Feature.Builder().withName("A").withFilePath("pathA").build().hashCode()
        )
        assertEquals(
            Feature.Builder().withName("A").withFilePath("pathA").build(),
            Feature.Builder().withName("A").withFilePath("pathA").build()
        )
    }
}