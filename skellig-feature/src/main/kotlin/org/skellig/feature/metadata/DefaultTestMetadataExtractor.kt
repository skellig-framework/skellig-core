package org.skellig.feature.metadata

class DefaultTestMetadataExtractor {

    private var testMetadataExtractors: Collection<TestMetadataExtractor<out TestMetadata<*>>>? = null
    private val tagDetailsExtractor = TagDetailsExtractor()

    init {
        testMetadataExtractors = listOf(tagDetailsExtractor)
    }

    fun extractFrom(text: String?): List<TestMetadata<*>>? {
        val preRequisites: List<TestMetadata<*>> = testMetadataExtractors!!
                .mapNotNull { it.extractFrom(text) }
                .toList()
        return preRequisites.ifEmpty { null }
    }

    fun extractTags(text: String?): Set<String>? {
        val tags = tagDetailsExtractor.extractFrom(text)
        return tags?.getDetails()?.tags
    }
}