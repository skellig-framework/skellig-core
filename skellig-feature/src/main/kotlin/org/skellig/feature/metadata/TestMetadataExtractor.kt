package org.skellig.feature.metadata

interface TestMetadataExtractor<T> {

    fun extractFrom(text: String?): TestMetadata<T>?
}