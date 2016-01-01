package org.skellig.runner.tagextractor;

import org.skellig.feature.TestPreRequisites;

import java.util.Collection;
import java.util.Optional;

public class RequestedTagExtractor implements TagExtractor {

    @Override
    public <T> Optional<T> extract(Class<T> tagClass, Collection<TestPreRequisites<?>> testPreRequisites) {
        return testPreRequisites.stream()
                .filter(item -> tagClass.equals(item.getDetails().getClass()))
                .map(item -> (T)item.getDetails())
                .findFirst();
    }
}
