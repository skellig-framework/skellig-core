package org.skellig.runner.tagextractor

import org.skellig.feature.metadata.TestMetadata

interface TagExtractor {

    fun <T> extract(tagClass: Class<T>, testMetadata: Collection<TestMetadata<*>>): T?
}