package org.skellig.feature.metadata

/**
 * Represents a class for filtering tags, used in filtering hooks, features or test scenarios.
 *
 * @property includeTags The set of tags to include hooks, features or test scenarios.
 * @property excludeTags The set of tags to exclude hooks, features or test scenarios.
 */
class TagsFilter(
    private val includeTags: Set<String>,
    private val excludeTags: Set<String>
) {
    fun checkTagsAreIncluded(tagsToCheck: Set<String>?): Boolean {
        val isNotExcluded = if (excludeTags.isNotEmpty()) tagsToCheck?.intersect(excludeTags)?.isEmpty() ?: true else true
        return if (isNotExcluded && includeTags.isNotEmpty()) tagsToCheck?.intersect(includeTags)?.isNotEmpty() ?: false
        else isNotExcluded
    }
}