package org.skellig.teststep.processing.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultTestDataConverter implements TestDataConverter {

    private List<TestDataConverter> testDataConverters;

    protected DefaultTestDataConverter(List<TestDataConverter> valueConverters) {
        this.testDataConverters = valueConverters;
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

            CsvTestDataConverter csvTestDataConverter = new CsvTestDataConverter(classLoader);
            testDataConverters.add(new TemplateTestDataConverter(classLoader, csvTestDataConverter));
            testDataConverters.add(csvTestDataConverter);

            return new DefaultTestDataConverter(testDataConverters);
        }
    }
}
