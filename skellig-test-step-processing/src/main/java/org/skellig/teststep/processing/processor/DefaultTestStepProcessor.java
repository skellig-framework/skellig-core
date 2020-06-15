package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.converter.DateValueConverter;
import org.skellig.teststep.processing.converter.FileValueConverter;
import org.skellig.teststep.processing.converter.IncrementValueConverter;
import org.skellig.teststep.processing.converter.TestStepStateValueConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;
import org.skellig.teststep.reader.model.TestStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultTestStepProcessor implements TestStepProcessor {

    private List<TestStepValueConverter> valueConverters;
    private List<TestStepProcessor> testStepProcessors;

    protected DefaultTestStepProcessor(List<TestStepValueConverter> valueConverters,
                                       List<TestStepProcessor> testStepProcessors) {
        this.valueConverters = valueConverters;
        this.testStepProcessors = testStepProcessors;
    }

    @Override
    public void process(TestStep testStep, Map<String, String> parameters) {
        //TODO: apply parameters and run pre-processing

    }

    @Override
    public Class<? extends TestStep> getTestStepClass() {
        return TestStep.class;
    }

    public static class Builder {

        private TestStepStateValueConverter.Builder builderForStateValueConverter;
        private List<TestStepValueConverter> valueConverters;
        private List<TestStepProcessor> testStepProcessors;
        private TestScenarioState testScenarioState;
        private ClassLoader classLoader;

        public Builder() {
            valueConverters = new ArrayList<>();
            testStepProcessors = new ArrayList<>();
            builderForStateValueConverter = new TestStepStateValueConverter.Builder();
        }

        public Builder withTestStepProcessors(TestStepProcessor testStepProcessor) {
            this.testStepProcessors.add(testStepProcessor);
            return this;
        }

        public Builder withValueConverter(TestStepValueConverter valueConverter) {
            this.valueConverters.add(valueConverter);
            return this;
        }

        public Builder withValueOfStateExtractor(TestStepValueExtractor valueExtractor) {
            builderForStateValueConverter.withValueExtractor(valueExtractor);
            return this;
        }

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public TestStepProcessor build() {
            Objects.requireNonNull(classLoader, "ClassLoader must be provided");
            Objects.requireNonNull(testScenarioState, "TestScenarioState must be provided");

            valueConverters.add(builderForStateValueConverter.build(testScenarioState));
            valueConverters.add(new DateValueConverter());
            valueConverters.add(new FileValueConverter(classLoader));
            valueConverters.add(new IncrementValueConverter());

            return new DefaultTestStepProcessor(valueConverters, testStepProcessors);
        }
    }
}
