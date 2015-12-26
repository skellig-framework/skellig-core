package org.skellig.feature.parser;

import org.skellig.feature.TestPreRequisites;

interface TestPreRequisitesExtractor<T> {

    TestPreRequisites<T> extractFrom(String text);
}