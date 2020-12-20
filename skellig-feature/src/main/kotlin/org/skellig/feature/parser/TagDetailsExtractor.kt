package org.skellig.feature.parser

import org.skellig.feature.TagDetails
import org.skellig.feature.TestPreRequisites
import java.util.regex.Pattern

class TagDetailsExtractor : TestPreRequisitesExtractor<TagDetails> {

    companion object {
        private val TAGS_PATTERN = Pattern.compile("@([\\w-_]+)")
        private val SPECIAL_TAGS_IGNORE_FILTER = setOf("Init", "Data")
    }

    override fun extractFrom(text: String?): TestPreRequisites<TagDetails>? {
        return text?.let {
            val tags = mutableSetOf<String>()
            val matcher = TAGS_PATTERN.matcher(text)
            while (matcher.find()) {
                val tag = matcher.group(1)
                if (!SPECIAL_TAGS_IGNORE_FILTER.contains(tag)) {
                    tags.add(tag)
                }
            }
            if (tags.isEmpty()) null else TagDetails(tags)
        }
    }
}