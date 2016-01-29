package org.skellig.teststep.processing.runner;

import org.skellig.teststep.processing.converter.DateValueConverter;
import org.skellig.teststep.processing.converter.FileValueConverter;
import org.skellig.teststep.processing.converter.IncrementValueConverter;
import org.skellig.teststep.processing.converter.TestStepStateValueConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.model.TestStep;
import org.skellig.teststep.reader.model.TestStepFileExtension;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultTestStepRunner implements TestStepRunner {

    private TestStepProcessor testStepProcessor;
    private TestStepsRegistry testStepsRegistry;
    private List<TestStepValueConverter> valueConverters;

    protected DefaultTestStepRunner(TestStepProcessor testStepProcessor,
                                    TestStepsRegistry testStepsRegistry,
                                    List<TestStepValueConverter> valueConverters) {
        this.testStepProcessor = testStepProcessor;
        this.testStepsRegistry = testStepsRegistry;
        this.valueConverters = valueConverters;
    }

    @Override
    public void run(String testStepName) {
        run(testStepName, Collections.emptyMap());
    }

    @Override
    public void run(String testStepName, Map<String, String> parameters) {
        Optional<TestStep> testStep = testStepsRegistry.getByName(testStepName);

        if (testStep.isPresent()) {
            Map<String, String> additionalParameters =
                    testStepsRegistry.extractParametersFromTestStepName(testStep.get(), testStepName);
            additionalParameters.putAll(parameters);
            //TODO: apply parameters
            testStepProcessor.process(testStep.get());
        } else {
            throw new TestStepProcessingException(
                    String.format("Test step '%s' is not found in any of registered test data files from: %s",
                            testStepName, testStepsRegistry.getTestStepsRootPath()));
        }
    }

    public static class Builder {

        private TestStepStateValueConverter.Builder builderForStateValueConverter;
        private List<TestStepValueConverter> valueConverters;
        private TestStepProcessor testStepProcessor;
        private TestStepReader testStepReader;
        private TestScenarioState testScenarioState;
        private Collection<Path> testStepPaths;
        private ClassLoader classLoader;

        public Builder() {
            valueConverters = new ArrayList<>();
            builderForStateValueConverter = new TestStepStateValueConverter.Builder();
        }

        public Builder withTestStepProcessor(TestStepProcessor testStepProcessor) {
            this.testStepProcessor = testStepProcessor;
            return this;
        }

        public Builder withValueConverter(TestStepValueConverter valueConverter) {
            this.valueConverters.add(valueConverter);
            return this;
        }

        public Builder withValueExtractor(TestStepValueExtractor valueExtractor) {
            builderForStateValueConverter.withValueExtractor(valueExtractor);
            return this;
        }

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withTestStepReader(TestStepReader testStepReader, Collection<Path> testStepPaths) {
            this.testStepReader = testStepReader;
            this.testStepPaths = testStepPaths;
            return this;
        }


        public Builder withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public TestStepRunner build() {
            Objects.requireNonNull(classLoader, "ClassLoader must be provided");
            Objects.requireNonNull(testStepReader, "Test Step Reader is mandatory");
            Objects.requireNonNull(testStepProcessor, "Test Step processor is mandatory");

            valueConverters.add(builderForStateValueConverter.build(testScenarioState));
            valueConverters.add(new DateValueConverter());
            valueConverters.add(new FileValueConverter(classLoader));
            valueConverters.add(new IncrementValueConverter());

            TestStepsRegistry testStepsRegistry = new TestStepsRegistry(TestStepFileExtension.STS, testStepReader);
            testStepsRegistry.registerFoundTestStepsInPath(testStepPaths);

            return new DefaultTestStepRunner(testStepProcessor, testStepsRegistry, valueConverters);
        }
    }
}
