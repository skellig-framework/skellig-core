package org.skellig.teststep.processing.converter;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TableTestStepValueExtractor implements TestStepValueExtractor {

    private static final Pattern PATH_SEPARATOR = Pattern.compile("\\.");
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");

    @Override
    public Object extract(Object value, String filter) {
        for (String key : PATH_SEPARATOR.split(filter)) {
            if (value instanceof Map) {
                value = ((Map) value).get(key);
            } else if (value instanceof List) {
                Matcher matcher = INDEX_PATTERN.matcher(key);
                if (matcher.find()) {
                    int index = Integer.parseInt(matcher.group(1));
                    value = ((List) value).get(index);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return value;
    }

    @Override
    public String getExtractFunctionName() {
        return "table";
    }

}
