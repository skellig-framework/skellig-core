package org.skellig.teststep.processing.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class DefaultTestStepFactory extends BaseTestStepFactory {

    private Collection<TestStepFactory> factories;

    public DefaultTestStepFactory(Properties keywordsProperties,
                                  TestStepValueConverter testStepValueConverter,
                                  Collection<TestStepFactory> factories,
                                  TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
        this.factories = factories;
    }

    @Override
    public TestStep create(String testStepName, Map<String, Object> rawTestStep, Map<String, String> parameters) {
        return factories.stream()
                .filter(factory -> factory.isConstructableFrom(rawTestStep))
                .findFirst()
                .map(factory -> factory.create(testStepName, rawTestStep, parameters))
                .orElse(super.create(testStepName, rawTestStep, parameters));
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return new TestStep.Builder();
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return true;
    }

    public static class Builder {

        private Collection<TestStepFactory> testStepFactories;
        private Properties keywordsProperties;
        private TestStepValueConverter testStepValueConverter;
        private TestDataConverter testDataConverter;

        public Builder() {
            testStepFactories = new ArrayList<>();
        }

        public Builder withTestStepFactory(TestStepFactory factory) {
            this.testStepFactories.add(factory);
            return this;
        }

        public Builder withKeywordsProperties(Properties keywordsProperties) {
            this.keywordsProperties = keywordsProperties;
            return this;
        }

        public Builder withTestStepValueConverter(TestStepValueConverter testStepValueConverter) {
            this.testStepValueConverter = testStepValueConverter;
            return this;
        }

        public Builder withTestDataConverter(TestDataConverter testDataConverter) {
            this.testDataConverter = testDataConverter;
            return this;
        }

        public TestStepFactory build() {
            return new DefaultTestStepFactory(keywordsProperties, testStepValueConverter,
                    testStepFactories, testDataConverter);
        }
    }
}
