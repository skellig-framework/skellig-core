package org.skellig.feature.metadata

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TagsFilterTest {

    @Test
    fun testCheckTagsAreIncluded() {
        val tagsFilter = TagsFilter(setOf("tag1", "tag2"), setOf("tag3"))

        val result1 = tagsFilter.checkTagsAreIncluded(setOf("tag1", "tag2"))
        val result2 = tagsFilter.checkTagsAreIncluded(setOf("tag3"))
        val result3 = tagsFilter.checkTagsAreIncluded(setOf("tag4"))
        val result4 = tagsFilter.checkTagsAreIncluded(null)

        assertTrue(result1)
        assertFalse(result2)
        assertFalse(result3)
        assertFalse(result4)
    }

    @Test
    fun testCheckTagsAreIncludedWithEmptyIncludeTags() {
        val tagsFilter = TagsFilter(emptySet(), setOf("tag3"))

        val result1 = tagsFilter.checkTagsAreIncluded(setOf("tag1", "tag2"))
        val result2 = tagsFilter.checkTagsAreIncluded(setOf("tag3"))
        val result3 = tagsFilter.checkTagsAreIncluded(setOf("tag4"))
        val result4 = tagsFilter.checkTagsAreIncluded(null)

        assertTrue(result1)
        assertFalse(result2)
        assertTrue(result3)
        assertTrue(result4)
    }

    @Test
    fun testCheckTagsAreIncludedWithEmptyExcludeTags() {
        val tagsFilter = TagsFilter(setOf("tag1", "tag2"), emptySet())

        val result1 = tagsFilter.checkTagsAreIncluded(setOf("tag1", "tag2"))
        val result2 = tagsFilter.checkTagsAreIncluded(setOf("tag3"))
        val result3 = tagsFilter.checkTagsAreIncluded(setOf("tag4"))
        val result4 = tagsFilter.checkTagsAreIncluded(null)

        assertTrue(result1)
        assertFalse(result2)
        assertFalse(result3)
        assertFalse(result4)
    }

    @Test
    fun testCheckTagsAreIncludedWithEmptyIncludeAndExcludeTags() {
        val tagsFilter = TagsFilter(emptySet(), emptySet())

        val result1 = tagsFilter.checkTagsAreIncluded(setOf("tag1", "tag2"))
        val result2 = tagsFilter.checkTagsAreIncluded(setOf("tag3"))
        val result3 = tagsFilter.checkTagsAreIncluded(setOf("tag4"))
        val result4 = tagsFilter.checkTagsAreIncluded(null)

        assertTrue(result1)
        assertTrue(result2)
        assertTrue(result3)
        assertTrue(result4)
    }
}