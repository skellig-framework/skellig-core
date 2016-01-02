package org.skellig.teststep.reader.model;

import java.util.regex.Pattern;
import java.util.stream.Stream;

public enum ValidationType {
    TABLE(null),
    PLAIN(Pattern.compile("response\\(\\)")),
    XPATH(Pattern.compile("xpath\\((.+)\\)")),
    JSON_PATH(Pattern.compile("json_path\\((.+)\\)"));

    private Pattern pattern;

    ValidationType(Pattern pattern) {
        this.pattern = pattern;
    }

    public static ValidationType getValidationTypeFor(String actualValue) {
        return Stream.of(values())
                .filter(validationType -> validationType.pattern != null)
                .filter(validationType -> validationType.pattern.matcher(actualValue).matches())
                .findFirst()
                .orElse(ValidationType.TABLE);
    }
}