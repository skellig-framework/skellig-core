package org.skellig.feature.metadata

class DefaultTestMetadataExtractor {

    private var testMetadataExtractors: Collection<TestMetadataExtractor<out TestMetadata<*>>>? = null
    private val tagDetailsExtractor = TagDetailsExtractor()

    init {
        testMetadataExtractors = listOf(tagDetailsExtractor)
    }

    fun extractTags(text: String?): Set<String>? {
        val tags = tagDetailsExtractor.extractFrom(text)
        return tags?.getDetails()?.tags
    }
}