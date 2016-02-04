package org.skellig.teststep.processing.model.factory;

import org.apache.commons.lang3.StringUtils;
import org.skellig.teststep.processing.converter.TestStepValueConverter;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestStepFactoryValueConverter {

    private static final Pattern PARAMETER_REGEX = Pattern.compile("\\$\\{([\\w-_]+)(\\s*:\\s*(.+))?\\}");

    private TestStepValueConverter testStepValueConverter;

    TestStepFactoryValueConverter(TestStepValueConverter testStepValueConverter) {
        this.testStepValueConverter = testStepValueConverter;
    }

    <T> T convertValue(Object value, Map<String, Object> parameters) {
        Object result = value;
        if (isString(value)) {
            result = applyParameters(String.valueOf(value), parameters);
            if (isString(result)) {
                result = testStepValueConverter.convert(String.valueOf(result));
            }
        }
        return (T) result;
    }

    private Object applyParameters(String valueAsString, Map<String, Object> parameters) {
        Matcher matcher = PARAMETER_REGEX.matcher(valueAsString);
        Object result = valueAsString;
        if (matcher.find()) {
            String parameterName = matcher.group(1);
            Object parameterValue = parameters.getOrDefault(parameterName, null);
            boolean hasDefaultValue = matcher.group(3) != null;
            if (isString(parameterValue)) {
                String parameterValueAsString = String.valueOf(parameterValue);
                if (StringUtils.isNotEmpty(parameterValueAsString) || !hasDefaultValue) {
                    result = valueAsString.replace(matcher.group(0), parameterValueAsString);
                } else {
                    String defaultValue = matcher.group(3);
                    defaultValue = String.valueOf(convertValue(defaultValue, parameters));

                    result = valueAsString.replace(matcher.group(0), defaultValue);
                }
            } else {
                if (parameterValue != null || !hasDefaultValue) {
                    result = parameterValue;
                } else {
                    result = convertValue(matcher.group(3), parameters);
                }
            }
        }
        return result;
    }

    private boolean isString(Object value) {
        return value instanceof String;
    }

}