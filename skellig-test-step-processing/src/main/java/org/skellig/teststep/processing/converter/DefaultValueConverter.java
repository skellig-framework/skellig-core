package org.skellig.teststep.processing.converter;

import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class DefaultValueConverter implements TestStepValueConverter {

    private List<TestStepValueConverter> valueConverters;

    protected DefaultValueConverter(List<TestStepValueConverter> valueConverters) {
        this.valueConverters = valueConverters;
    }

    @Override
    public Object convert(String value) {
        Object result = value;
        for (TestStepValueConverter valueConverter : valueConverters) {
            if (result != null && result.getClass().equals(String.class)) {
                result = valueConverter.convert((String) result);
            }
        }
        return result;
    }

    public static class Builder {

        private List<TestStepValueConverter> valueConverters;
        private TestScenarioState testScenarioState;
        private TestStepValueExtractor testStepValueExtractor;
        private Function<String, String> getPropertyFunction;
        private ClassLoader classLoader;

        public Builder() {
            valueConverters = new ArrayList<>();
        }

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withGetPropertyFunction(Function<String, String> getPropertyFunction) {
            this.getPropertyFunction = getPropertyFunction;
            return this;
        }

        public Builder withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder withTestStepValueExtractor(TestStepValueExtractor testStepValueExtractor) {
            this.testStepValueExtractor = testStepValueExtractor;
            return this;
        }

        public Builder withValueConverter(TestStepValueConverter valueConverter) {
            this.valueConverters.add(valueConverter);
            return this;
        }

        public TestStepValueConverter build() {
            Objects.requireNonNull(testScenarioState, "Test Scenario State must be provided");

            valueConverters.add(0, new PropertyValueConverter(valueConverters, getPropertyFunction));
            withValueConverter(new TestStepStateValueConverter(testScenarioState, testStepValueExtractor));
            if (classLoader != null) {
                withValueConverter(new FileValueConverter(classLoader));
            }
            withValueConverter(new NumberValueConverter());
            withValueConverter(new IncrementValueConverter());
            withValueConverter(new DateTimeValueConverter());

            return new DefaultValueConverter(valueConverters);
        }
    }
}
