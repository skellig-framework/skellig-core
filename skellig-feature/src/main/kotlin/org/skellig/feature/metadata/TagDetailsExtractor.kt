package org.skellig.feature.metadata

import java.util.regex.Pattern

class TagDetailsExtractor : TestMetadataExtractor<TagDetails> {

    companion object {
        private val TAGS_PATTERN = Pattern.compile("@([\\w-_]+)")
    }

    override fun extractFrom(text: String?): TestMetadata<TagDetails>? {
        return text?.let {
            val tags = mutableSetOf<String>()
            val matcher = TAGS_PATTERN.matcher(text)
            while (matcher.find()) {
                tags.add(matcher.group(1))
            }
            if (tags.isEmpty()) null else TagDetails(tags)
        }
    }
}