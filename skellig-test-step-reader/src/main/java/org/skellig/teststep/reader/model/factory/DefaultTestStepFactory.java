package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.exception.TestStepReadException;
import org.skellig.teststep.reader.model.TestStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class DefaultTestStepFactory implements TestStepFactory {

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
                .orElseThrow(() ->
                        new TestStepReadException("No Test Step Factory found for raw test step: " + rawTestStep));
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
            addLastTestStepFactory();
            return new DefaultTestStepFactory(factories);
        }

        private void addLastTestStepFactory() {
            factories.add(new ValidationTestStepFactory());
        }
    }
}
