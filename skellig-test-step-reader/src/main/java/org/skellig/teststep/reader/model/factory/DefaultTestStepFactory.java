package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.TestStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DefaultTestStepFactory extends BaseTestStepFactory {

    private Collection<TestStepFactory> factories;

    protected DefaultTestStepFactory(Collection<TestStepFactory> factories) {
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
            factories.add(new HttpTestStepFactory());
        }

        public Build withFactory(TestStepFactory factory) {
            this.factories.add(factory);
            return this;
        }

        public TestStepFactory build() {
            return new DefaultTestStepFactory(factories);
        }
    }
}
