package org.skellig.teststep.processing.model.factory;

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
                                  Collection<TestStepFactory> factories) {
        super(keywordsProperties, testStepValueConverter);
        this.factories = factories;
    }


    @Override
    public TestStep create(Map<String, Object> rawTestStep) {
        return factories.stream()
                .filter(factory -> factory.isConstructableFrom(rawTestStep))
                .findFirst()
                .map(factory -> factory.create(rawTestStep))
                .orElse(new TestStep.Builder()
                        .withId(getId(rawTestStep))
                        .withName(getName(rawTestStep))
                        .withTestData(getTestData(rawTestStep))
                        .withValidationDetails(createValidationDetails(rawTestStep))
                        .build());
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return true;
    }

    public static class Builder {

        private Collection<TestStepFactory> testStepFactories;
        private Properties keywordsProperties;
        private TestStepValueConverter testStepValueConverter;

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

        public TestStepFactory build() {
            return new DefaultTestStepFactory(keywordsProperties, testStepValueConverter, testStepFactories);
        }
    }
}
