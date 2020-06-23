package org.skellig.teststep.processing.validation.comparator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexValueComparator implements ValueComparator {

    private static final Pattern PATTERN = Pattern.compile("regex\\((.+)\\)");
    private static final String REGEX_PREFIX = "regex(";

    @Override
    public boolean compare(Object expectedValue, Object actualValue) {
        if (actualValue != null) {
            Matcher matcher = PATTERN.matcher(String.valueOf(expectedValue));
            if (matcher.find()) {
                String regex = matcher.group(1);
                String actualValueAsString = String.valueOf(actualValue);
                return isMatchRegex(regex, actualValueAsString);
            }
        }
        return false;
    }

    private boolean isMatchRegex(String regex, String actualValueAsString) {
        try {
            Pattern expectedPattern = Pattern.compile(regex);
            return expectedPattern.matcher(actualValueAsString).matches();
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean isApplicable(Object expectedValue) {
        return String.valueOf(expectedValue).contains(REGEX_PREFIX);
    }
}
