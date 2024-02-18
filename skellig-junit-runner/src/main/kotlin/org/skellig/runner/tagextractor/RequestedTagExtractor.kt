package org.skellig.runner.tagextractor

import org.skellig.feature.metadata.TestMetadata

class RequestedTagExtractor : TagExtractor {

    override fun <T> extract(tagClass: Class<T>, testMetadata: Collection<TestMetadata<*>>): T? {
        return testMetadata
                .filter { tagClass == it.getDetails()!!.javaClass }
                .map { it.getDetails() as T }
                .firstOrNull()
    }
}