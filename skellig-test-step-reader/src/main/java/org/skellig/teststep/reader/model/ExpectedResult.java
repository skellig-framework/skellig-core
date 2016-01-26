package org.skellig.teststep.reader.model;

import org.skellig.teststep.reader.exception.ValidationException;

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

public class ExpectedResult {

    private final Pattern INDEX_PATTERN = Pattern.compile("(\\w*)\\[(\\d+)\\]");

    private String property;
    private Object expectedResult;
    private ValidationType validationType;
    private ExpectedResult parent;

    public ExpectedResult(String property, Object expectedResult,
                          ValidationType validationType, ExpectedResult parent) {
        this.property = property;
        this.expectedResult = expectedResult;
        this.validationType = validationType;
    }

    public ExpectedResult(String property, Object expectedResult, ValidationType validationType) {
        this(property, expectedResult, validationType, null);
    }

    public String getProperty() {
        return property;
    }

    public <T> T getExpectedResult() {
        return (T) expectedResult;
    }

    public ValidationType getValidationType() {
        return validationType;
    }

    public ValidationType getValidationTypeOfParent() {
        return parent != null ? parent.getValidationType() : ValidationType.ALL_MATCH;
    }

    public String getFullPropertyPath() {
        StringBuilder pathBuilder = new StringBuilder();
        constructFullPropertyPath(parent, pathBuilder);
        return pathBuilder.toString();
    }

    private void constructFullPropertyPath(ExpectedResult parent, StringBuilder pathBuilder) {
        if (parent.parent != null) {
            constructFullPropertyPath(parent.parent, pathBuilder);
        }
        pathBuilder.append(parent.getProperty());
        if (parent != this.parent) {
            pathBuilder.append('.');
        }
    }

    protected Object extract(Object actualResult) {
        String propertyName = property;
        int index = -1;
        if (property.endsWith("]")) {
            Matcher matcher = INDEX_PATTERN.matcher(property);
            if (matcher.find()) {
                propertyName = matcher.group(1);
                index = Integer.parseInt(matcher.group(2));
            }
        }

        if (actualResult instanceof Map) {
            Object propertyValue = ((Map) actualResult).get(propertyName);
            if (index > 0) {
                if (propertyValue instanceof List) {
                    actualResult = ((List) propertyValue).get(index);
                } else if (actualResult.getClass().isArray()) {
                    actualResult = Array.get(actualResult, index);
                }
            }
        } else if (actualResult instanceof List) {
            if (index > 0) {
                actualResult = ((List) actualResult).get(index);
            } else {
                actualResult = extractValueFromObject(propertyName, actualResult);
            }
        } else if (actualResult.getClass().isArray()) {
            actualResult = index > 0 ? Array.get(actualResult, index) : actualResult;
        } else {
            actualResult = extractValueFromObject(propertyName, actualResult);
        }

        return actualResult;
    }

    private Object extractValueFromObject(String propertyName, Object actualResult) {
        try {
            return getPropertyGetter(propertyName, actualResult.getClass()).invoke(actualResult);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ValidationException(String.format("Failed to call property getter %s of %s",
                    propertyName, actualResult), e);
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
            throw new ValidationException(String.format("Failed to get property %s of %s", propertyName, beanClass), e);
        }

        if (method == null) {
            throw new ValidationException(String.format("Property %s was not found in %s", propertyName, beanClass));
        } else {
            return method;
        }
    }
}