package org.skellig.teststep.processing.model;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public enum ValidationType {
    ALL_MATCH(Pattern.compile("all[\\s_]?match")),
    NONE_MATCH(Pattern.compile("none[\\s_]?match")),
    ANY_MATCH(Pattern.compile("any[\\s_]?match")),
    ANY_NONE_MATCH(Pattern.compile("any[\\s_]/none[\\s_]?match"));

    private Pattern pattern;

    ValidationType(Pattern pattern) {
        this.pattern = pattern;
    }

    public static ValidationType getValidationTypeFor(String actualValue) {
        return Stream.of(values())
                .filter(validationType -> validationType.pattern.matcher(actualValue).matches())
                .findFirst()
                .orElse(ValidationType.ALL_MATCH);
    }

    public Pattern getPattern() {
        return pattern;
    }
}