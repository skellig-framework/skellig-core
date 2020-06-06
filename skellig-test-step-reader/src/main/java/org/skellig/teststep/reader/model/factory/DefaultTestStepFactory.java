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
                .orElse(new TestStep.Builder<TestStep>()
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

    public static class Build {

        private Collection<TestStepFactory> factories;

        public Build() {
            factories = new ArrayList<>();
        }

        public Build withFactory(TestStepFactory factory) {
            this.factories.add(factory);
            return this;
        }

        public Build withDefaultFactories(Properties testStepKeywordProperties) {
            this.factories.add(new HttpTestStepFactory(testStepKeywordProperties));
            this.factories.add(new DatabaseTestStepFactory(testStepKeywordProperties));
            return this;
        }

        public Build withDefaultFactories() {
            return withDefaultFactories(null);
        }

        public TestStepFactory build() {
            return new DefaultTestStepFactory(factories);
        }
    }
}
