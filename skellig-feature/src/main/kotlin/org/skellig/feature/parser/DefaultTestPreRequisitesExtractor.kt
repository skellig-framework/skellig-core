package org.skellig.feature.parser

import org.skellig.feature.TestPreRequisites

class DefaultTestPreRequisitesExtractor {

    private var testPreRequisitesExtractors: Collection<TestPreRequisitesExtractor<out TestPreRequisites<*>>>? = null
    private val tagDetailsExtractor = TagDetailsExtractor()

    init {
        testPreRequisitesExtractors = listOf(tagDetailsExtractor)
    }

    fun extractFrom(text: String?): List<TestPreRequisites<*>>? {
        val preRequisites: List<TestPreRequisites<*>> = testPreRequisitesExtractors!!
                .mapNotNull { it.extractFrom(text) }
                .toList()
        return if (preRequisites.isEmpty()) null else preRequisites
    }

    fun extractTags(text: String?): Set<String>? {
        val tags = tagDetailsExtractor.extractFrom(text)
        return tags?.getDetails()?.tags
    }
}