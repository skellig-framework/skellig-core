package org.skellig.teststep.processing.validation.comparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ContainsValueComparator implements ValueComparator {

    private static final Pattern CONTAINS_PATTERN = Pattern.compile("contains\\((.+)\\)");

    @Override
    public boolean compare(Object actualValue, Object expectedValue) {
        if (actualValue != null) {
            Matcher matcher = CONTAINS_PATTERN.matcher(String.valueOf(expectedValue));
            if (matcher.find()) {
                String expectedValueAsString = matcher.group(1);
                // usually actual is String so to speed up comparison it checks if it is String first
                if (actualValue.getClass().equals(String.class)) {
                    return ((String) actualValue).contains(expectedValueAsString);
                } else if (actualValue.getClass().isArray()) {
                    return Arrays.stream((Object[]) actualValue)
                            .map(String::valueOf)
                            .anyMatch(item -> item.equals(expectedValueAsString));
                } else if (actualValue instanceof Collection) {
                    return ((Collection<Object>) actualValue).stream()
                            .map(String::valueOf)
                            .anyMatch(item -> item.equals(expectedValueAsString));
                } else {
                    return String.valueOf(actualValue).contains(expectedValueAsString);
                }
            }
        }
        return false;
    }

    @Override
    public boolean isApplicable(Object expectedValue) {
        return CONTAINS_PATTERN.matcher(String.valueOf(expectedValue)).matches();
    }
}

