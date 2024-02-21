package org.skellig.feature.metadata

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