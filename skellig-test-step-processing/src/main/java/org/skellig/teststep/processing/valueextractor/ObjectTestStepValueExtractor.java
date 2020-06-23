package org.skellig.teststep.processing.valueextractor;

import org.skellig.teststep.processing.exception.ValueExtractionException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ObjectTestStepValueExtractor implements TestStepValueExtractor {

    private static final Pattern PATH_SEPARATOR = Pattern.compile("\\.");
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");

    @Override
    public Object extract(Object value, String extractionParameter) {
        for (String key : PATH_SEPARATOR.split(extractionParameter)) {
            if (value instanceof Map) {
                value = extractValueFromMap(value, key);
            } else if (value instanceof List || (value != null && value.getClass().isArray())) {
                value = extractValueFromListOrArray(value, key);
            } else if (value != null) {
                value = extractValueFromObject(key, value);
            }
        }
        return value;
    }

    private Object extractValueFromListOrArray(Object value, String key) {
        int index = getIndex(key);
        if (index >= 0) {
            if (value.getClass().isArray()) {
                value = Array.get(value, index);
            } else {
                value = ((List) value).get(index);
            }
        } else {
            value = extractValueFromObject(key, value);
        }
        return value;
    }

    private Object extractValueFromMap(Object value, String key) {
        Map valueAsMap = (Map) value;
        if (valueAsMap.containsKey(key)) {
            value = valueAsMap.get(key);
        } else {
            try {
                value = extractValueFromObject(key, value);
            } catch (ValueExtractionException ex) {
                value = null;
            }
        }
        return value;
    }

    private int getIndex(String value) {
        Matcher matcher = INDEX_PATTERN.matcher(value);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return -1;
        }
    }

    private Object extractValueFromObject(String propertyName, Object actualResult) {

        Object result = null;
        Method propertyGetter = getPropertyGetter(propertyName, actualResult.getClass());
        if (propertyGetter != null) {
            try {
                result = propertyGetter.invoke(actualResult);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ValueExtractionException(String.format("Failed to call property getter %s of %s",
                        propertyName, actualResult), e);
            }
        } else {
            Method method = getMethod(propertyName, actualResult.getClass());
            if (method != null) {
                try {
                    result = method.invoke(actualResult);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ValueExtractionException(String.format("Failed to call method %s of %s",
                            propertyName, actualResult), e);
                }
            }
        }

        if (result == null) {
            throw new ValueExtractionException(String.format("Failed to find property or method '%s' of %s",
                    propertyName, actualResult.getClass()));
        } else {
            return result;
        }
    }

    private Method getMethod(String propertyName, Class resultClass) {
        try {
            return resultClass.getMethod(propertyName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Method getPropertyGetter(String propertyName, Class beanClass) {
        Method method = null;
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(beanClass).getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && propertyName.equals(pd.getName())) {
                    method = pd.getReadMethod();
                    break;
                }
            }
        } catch (IntrospectionException e) {
            throw new ValueExtractionException(String.format("Failed to get property %s of %s", propertyName, beanClass), e);
        }

        return method;
    }

    @Override
    public String getExtractFunctionName() {
        return "";
    }

}
