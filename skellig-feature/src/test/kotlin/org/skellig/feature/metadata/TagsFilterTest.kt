package org.skellig.feature.metadata

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TagsFilterTest {

    @Test
    fun testTagsAndFilterEmpty() {
        assertTrue(TagsFilter(emptySet(), emptySet()).checkTagsAreIncluded(emptySet()))
        assertTrue(TagsFilter(emptySet(), emptySet()).checkTagsAreIncluded(null))
    }

    @Test
    fun testTagsEmptyAndFilterHasIncludeTags() {
        assertFalse(TagsFilter(setOf("t1"), emptySet()).checkTagsAreIncluded(emptySet()))
        assertFalse(TagsFilter(setOf("t1"), emptySet()).checkTagsAreIncluded(null))
    }

    @Test
    fun testTagsEmptyAndFilterHasExcludeTags() {
        assertTrue(TagsFilter(emptySet(), setOf("t1")).checkTagsAreIncluded(emptySet()))
        assertTrue(TagsFilter(emptySet(), setOf("t1")).checkTagsAreIncluded(null))
    }

    @Test
    fun testHasTagsAndFilterHasIncludeTags() {
        assertTrue(TagsFilter(setOf("t1", "t2"), emptySet()).checkTagsAreIncluded(setOf("t1")))
        assertFalse(TagsFilter(setOf("t2"), emptySet()).checkTagsAreIncluded(setOf("t1")))
    }

    @Test
    fun testHasTagsAndFilterHasExcludeTags() {
        assertFalse(TagsFilter(emptySet(), setOf("t2", "t1")).checkTagsAreIncluded(setOf("t1")))
        assertTrue(TagsFilter(emptySet(), setOf("t2")).checkTagsAreIncluded(setOf("t1")))
    }

    @Test
    fun testHasTagsAndFilterEmpty() {
        assertTrue(TagsFilter(emptySet(), emptySet()).checkTagsAreIncluded(setOf("t1")))
    }

    @Test
    fun testFilterHasBothTags() {
        assertFalse(TagsFilter(setOf("t1"), setOf("t2", "t1")).checkTagsAreIncluded(setOf("t1")))
        assertFalse(TagsFilter(setOf("t1"), setOf("t2")).checkTagsAreIncluded(emptySet()))
        assertFalse(TagsFilter(setOf("t1"), setOf("t2")).checkTagsAreIncluded(null))
        assertTrue(TagsFilter(setOf("t1"), setOf("t2")).checkTagsAreIncluded(setOf("t1")))
        assertFalse(TagsFilter(setOf("t3"), setOf("t2")).checkTagsAreIncluded(setOf("t1")))
        assertTrue(TagsFilter(setOf("t3", "t2", "t6"), emptySet()).checkTagsAreIncluded(setOf("t1", "t3")))
        assertFalse(TagsFilter(setOf("t3"), setOf("t2", "t4", "t3")).checkTagsAreIncluded(setOf("t1", "t3")))
    }
}