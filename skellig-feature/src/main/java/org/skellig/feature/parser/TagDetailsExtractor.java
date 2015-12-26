package org.skellig.feature.parser;

import org.skellig.feature.TagDetails;
import org.skellig.feature.TestPreRequisites;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TagDetailsExtractor implements TestPreRequisitesExtractor<TagDetails> {

    private static final Pattern TAGS_PATTERN = Pattern.compile("@([\\w-_]+)");
    private static final Set<String> SPECIAL_TAGS_IGNORE_FILTER = Stream.of("Init", "Data").collect(Collectors.toSet());

        @Override
        public TestPreRequisites<TagDetails> extractFrom(String text) {
            Set<String> tags = new HashSet<>();
            Matcher matcher = TAGS_PATTERN.matcher(text);
            while (matcher.find()) {
                String tag = matcher.group(1);
                if (!SPECIAL_TAGS_IGNORE_FILTER.contains(tag)) {
                    tags.add(tag);
                }
            }
            return tags.isEmpty() ? null : new TagDetails(tags);
        }
    }