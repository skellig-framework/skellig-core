package org.skellig.teststep.processing.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class CachedPattern {

    private static final Map<String, Pattern> stepNamePatternsCache = new ConcurrentHashMap<>();

    public static Pattern compile(String regex) {
        return stepNamePatternsCache.computeIfAbsent(regex, v -> Pattern.compile(regex));
    }

}
