package org.skellig.teststep.processing.valueextractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegexTestStepValueExtractor implements TestStepValueExtractor {

    @Override
    public Object extract(Object value, String extractionParameter) {
        Matcher matcher = Pattern.compile(extractionParameter).matcher((String) value);
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
