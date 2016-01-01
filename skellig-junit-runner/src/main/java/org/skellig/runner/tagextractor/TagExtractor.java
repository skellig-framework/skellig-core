package org.skellig.runner.tagextractor;

import org.skellig.feature.TestPreRequisites;

import java.util.Collection;
import java.util.Optional;

public interface TagExtractor {

    <T> Optional<T> extract(Class<T> tagClass, Collection<TestPreRequisites<?>> testPreRequisites);
}
