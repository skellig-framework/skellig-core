package org.skellig.teststep.processing.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultTestDataConverter implements TestDataConverter {

    private List<TestDataConverter> testDataConverters;

    protected DefaultTestDataConverter(List<TestDataConverter> testDataConverters) {
        this.testDataConverters = testDataConverters;
    }

    @Override
    public Object convert(Object value) {
        Object result = value;
        for (TestDataConverter testDataConverter : testDataConverters) {
            if (result == value) {
                result = testDataConverter.convert(result);
            } else {
                break;
            }
        }
        return result;
    }

    public static class Builder {

        private List<TestDataConverter> testDataConverters;
        private ClassLoader classLoader;

        public Builder() {
            testDataConverters = new ArrayList<>();
        }

        public Builder withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder withTestDataConverter(TestDataConverter testDataConverter) {
            this.testDataConverters.add(testDataConverter);
            return this;
        }

        public TestDataConverter build() {
            Objects.requireNonNull(classLoader, "ClassLoader must be provided");

            TestDataFromCsvConverter testDataFromCsvConverter = new TestDataFromCsvConverter(classLoader);
            testDataConverters.add(new TestDataFromFTLConverter(classLoader, testDataFromCsvConverter));
            testDataConverters.add(testDataFromCsvConverter);
            testDataConverters.add(new TestDataToBytesConverter());

            return new DefaultTestDataConverter(testDataConverters);
        }
    }
}
