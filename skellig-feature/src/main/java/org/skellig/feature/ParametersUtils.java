package org.skellig.feature;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParametersUtils {

    private static final Pattern PARAM_REGEX = Pattern.compile("<([\\w_-]+)>");

    static String replaceParametersIfFound(String value, Map<String, String> data) {
        Matcher matcher = PARAM_REGEX.matcher(value);
        while (matcher.find()) {
            String paramNameWithBrackets = matcher.group(0);
            String paramName = matcher.group(1);
            String paramValue = data.getOrDefault(paramName, "");
            value = value.replace(paramNameWithBrackets, paramValue);
        }
        return value;
    }
}
