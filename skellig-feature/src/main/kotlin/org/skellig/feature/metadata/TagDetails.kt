package org.skellig.feature.metadata

class TagDetails(val tags: Set<String>?) : TestMetadata<TagDetails> {

    override fun getDetails(): TagDetails {
        return this
    }
}