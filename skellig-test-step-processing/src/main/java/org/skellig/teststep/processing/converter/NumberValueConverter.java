package org.skellig.teststep.processing.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NumberValueConverter implements TestStepValueConverter {

    private static final Pattern INTEGER_REGEX = Pattern.compile("^\\((int|long|short)\\) *(\\d+)$");
    private static final Pattern FLOAT_REGEX = Pattern.compile("^\\((float|double)\\) *(\\d+\\.\\d+)$");

    @Override
    public Object convert(String value) {
        Matcher intMatcher = INTEGER_REGEX.matcher(value);
        if (intMatcher.find()) {
            String type = intMatcher.group(1);
            String intValue = intMatcher.group(2);
            if (type.equals("int")) {
                return Integer.parseInt(intValue);
            } else {
                return Long.parseLong(intValue);
            }
        } else {
            Matcher floatMatcher = FLOAT_REGEX.matcher(value);
            if (floatMatcher.find()) {
                String type = floatMatcher.group(1);
                String floatValue = floatMatcher.group(2);
                if (type.equals("double")) {
                    return Double.parseDouble(floatValue);
                } else {
                    return Float.parseFloat(floatValue);
                }
            }
        }
        return value;
    }
}
