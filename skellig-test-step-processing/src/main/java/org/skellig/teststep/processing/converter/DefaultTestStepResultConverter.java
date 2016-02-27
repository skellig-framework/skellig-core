package org.skellig.teststep.processing.converter;

import java.util.HashMap;
import java.util.Map;

public class DefaultTestStepResultConverter implements TestStepResultConverter {

    private Map<String, TestStepResultConverter> testStepResultConverters;

    protected DefaultTestStepResultConverter(Map<String, TestStepResultConverter> testStepResultConverters) {
        this.testStepResultConverters = testStepResultConverters;
    }

    @Override
    public Object convert(String convertFunction, Object result) {
        if (testStepResultConverters.containsKey(convertFunction)) {
            return testStepResultConverters.get(convertFunction).convert(convertFunction, result);
        } else {
            return result;
        }
    }

    @Override
    public String getConvertFunctionName() {
        return "";
    }


    public static class Builder {

        private Map<String, TestStepResultConverter> testStepResultConverters;

        public Builder() {
            testStepResultConverters = new HashMap<>();
        }

        public Builder withTestStepResultConverter(TestStepResultConverter testStepResultConverter) {
            this.testStepResultConverters.put(testStepResultConverter.getConvertFunctionName(), testStepResultConverter);
            return this;
        }

        public TestStepResultConverter build() {
            testStepResultConverters.put("string", new TestStepResultToStringConverter());

            return new DefaultTestStepResultConverter(testStepResultConverters);
        }
    }
}
