package org.skellig.feature.parser

import org.skellig.feature.TestPreRequisites

interface TestPreRequisitesExtractor<T> {

    fun extractFrom(text: String?): TestPreRequisites<T>?
}