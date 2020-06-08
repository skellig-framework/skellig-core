package org.skellig.teststep.processing.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexValueExtractor implements ValueExtractor {

    @Override
    public Object extract(Object value, String filter) {
        Matcher matcher = Pattern.compile(filter).matcher((String) value);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return value;
        }
    }

    @Override
    public String getExtractFunctionName() {
        return "regex";
    }

}
