package org.skellig.runner.junit.report.model;

import org.apache.commons.lang3.StringUtils;
import org.skellig.teststep.processing.model.ExpectedResult;
import org.skellig.teststep.processing.model.TestStep;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestStepReportDetails {

    private String name;
    private Object originalTestStep;
    private Object result;
    private String errorLog;

    private TestStepReportDetails(String name, Object originalTestStep, Object result, String errorLog) {
        this.name = name;
        this.originalTestStep = originalTestStep;
        this.result = result;
        this.errorLog = errorLog;
    }

    public String getName() {
        return name;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public boolean isPassed() {
        return errorLog == null || errorLog.equals("");
    }

    public boolean isIgnored() {
        return originalTestStep == null && result == null;
    }

    public String getTestData() {
        String result = "";
        if (originalTestStep instanceof TestStep) {
            Object testData = ((TestStep) originalTestStep).getTestData();
            result = testData != null ? String.valueOf(testData) : "";
        }
        return result;
    }

    public String getValidationDetails() {
        StringBuilder stringBuilder = new StringBuilder();
        if (originalTestStep instanceof TestStep) {
            ((TestStep) originalTestStep).getValidationDetails()
                    .ifPresent(validationDetails -> constructValidationDetails(validationDetails.getExpectedResult(), stringBuilder));
        }
        return stringBuilder.toString();
    }

    public String getProperties() {
        if (originalTestStep != null && !originalTestStep.getClass().equals(TestStep.class) &&
                originalTestStep instanceof TestStep) {
            Map<String, Optional<Method>> properties = getPropertyGettersOfTestStep(originalTestStep.getClass());
            return properties.entrySet().stream()
                    .filter(entry -> entry.getValue().isPresent())
                    .map(this::getPropertyWithValue)
                    .collect(Collectors.joining("\n"));
        }
        return "";
    }

    public String getResult() {
        return result != null ? String.valueOf(result) : "";
    }

    private void constructValidationDetails(ExpectedResult expectedResult, StringBuilder stringBuilder) {
        if (StringUtils.isNotEmpty(expectedResult.getProperty())) {
            stringBuilder.append(expectedResult.getProperty()).append(": ");
        }
        if (expectedResult.getExpectedResult() instanceof List) {
            stringBuilder.append(getValidationType(expectedResult)).append("[\n");
            expectedResult.<List<ExpectedResult>>getExpectedResult()
                    .forEach(item -> constructValidationDetails(item, stringBuilder));
            stringBuilder.append("]\n");
        } else if (expectedResult.getExpectedResult() != null) {
            stringBuilder
                    .append(expectedResult.getExpectedResult().toString())
                    .append("\n");
        }
    }

    private String getValidationType(ExpectedResult expectedResult) {
        return expectedResult.getValidationType().name().toLowerCase();
    }

    private String getPropertyWithValue(Map.Entry<String, Optional<Method>> propertyGetterPair) {
        try {
            Object getterResponse = propertyGetterPair.getValue().get().invoke(originalTestStep);
            if (getterResponse instanceof Optional) {
                getterResponse = ((Optional<?>) getterResponse).orElse(null);
            }
            return getterResponse != null ? propertyGetterPair.getKey() + ": " + getterResponse : "";
        } catch (IllegalAccessException | InvocationTargetException e) {
            return e.getMessage();
        }
    }

    private Map<String, Optional<Method>> getPropertyGettersOfTestStep(Class beanClass) {
        return Stream.of(beanClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toMap(fieldName -> fieldName,
                        fieldName -> {
                            try {
                                return Stream.of(Introspector.getBeanInfo(beanClass).getPropertyDescriptors())
                                        .filter(pd -> pd.getReadMethod() != null && fieldName.equals(pd.getName()))
                                        .map(PropertyDescriptor::getReadMethod)
                                        .findFirst();
                            } catch (IntrospectionException e) {
                                return Optional.empty();
                            }
                        }));
    }

    public static class Builder {
        private String name;
        private Object originalTestStep;
        private Object result;
        private String errorLog;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withOriginalTestStep(Object originalTestStep) {
            this.originalTestStep = originalTestStep;
            return this;
        }

        public Builder withResult(Object result) {
            this.result = result;
            return this;
        }

        public Builder withErrorLog(String errorLog) {
            this.errorLog = errorLog;
            return this;
        }

        public TestStepReportDetails build() {
            return new TestStepReportDetails(name, originalTestStep, result, errorLog);
        }
    }
}