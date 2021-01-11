package org.skellig.runner.tagextractor

import org.skellig.feature.TestPreRequisites

interface TagExtractor {

    fun <T> extract(tagClass: Class<T>, testPreRequisites: Collection<TestPreRequisites<*>>): T?
}