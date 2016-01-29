package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.TestStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class DefaultTestStepFactory extends BaseTestStepFactory {

    private Collection<TestStepFactory> factories;

    private DefaultTestStepFactory(Collection<TestStepFactory> factories) {
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

        public Builder() {
            testStepFactories = new ArrayList<>();
        }

        public Builder withTestStepFactory(TestStepFactory factory) {
            this.testStepFactories.add(factory);
            return this;
        }

        public Builder withDefaultFactories(Properties testStepKeywordProperties) {
            this.testStepFactories.add(new HttpTestStepFactory(testStepKeywordProperties));
            this.testStepFactories.add(new DatabaseTestStepFactory(testStepKeywordProperties));
            return this;
        }

        public Builder withDefaultFactories() {
            return withDefaultFactories(null);
        }

        public TestStepFactory build() {
            return new DefaultTestStepFactory(testStepFactories);
        }
    }
}
