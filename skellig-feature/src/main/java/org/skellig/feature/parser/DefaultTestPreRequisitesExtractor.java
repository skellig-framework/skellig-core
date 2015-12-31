package org.skellig.feature.parser;

import org.skellig.feature.TagDetails;
import org.skellig.feature.TestPreRequisites;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DefaultTestPreRequisitesExtractor {

    private Collection<TestPreRequisitesExtractor<? extends TestPreRequisites<?>>> testPreRequisitesExtractors;
    private TagDetailsExtractor tagDetailsExtractor = new TagDetailsExtractor();

    public DefaultTestPreRequisitesExtractor() {
        testPreRequisitesExtractors =
                Stream.of(
                        tagDetailsExtractor,
                        new InitDetailsExtractor(),
                        new DataDetailsExtractor()
                ).collect(Collectors.toList());
    }

    public List<TestPreRequisites<?>> extractFrom(String text) {
        List<TestPreRequisites<?>> preRequisites =
                testPreRequisitesExtractors.stream()
                        .map(extractor -> extractor.extractFrom(text))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        return preRequisites.isEmpty() ? null : preRequisites;
    }

    public Set<String> extractTags(String text) {
        final TestPreRequisites<TagDetails> tags = tagDetailsExtractor.extractFrom(text);
        return tags != null ? tags.getDetails().getTags() : null;
    }
}
