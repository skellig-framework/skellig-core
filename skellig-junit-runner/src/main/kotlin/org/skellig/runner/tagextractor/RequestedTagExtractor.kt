package org.skellig.runner.tagextractor

import org.skellig.feature.TestPreRequisites

class RequestedTagExtractor : TagExtractor {

    override fun <T> extract(tagClass: Class<T>, testPreRequisites: Collection<TestPreRequisites<*>>): T? {
        return testPreRequisites
                .filter { tagClass == it.getDetails()!!.javaClass }
                .map { it.getDetails() as T }
                .first()
    }
}