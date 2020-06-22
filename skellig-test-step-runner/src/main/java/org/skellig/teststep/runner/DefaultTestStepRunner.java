package org.skellig.teststep.runner;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.TestStepFactory;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.runner.model.TestStepFileExtension;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultTestStepRunner implements TestStepRunner {

    private TestStepProcessor<TestStep> testStepProcessor;
    private TestStepsRegistry testStepsRegistry;
    private TestStepFactory testStepFactory;
    private TestScenarioState testScenarioState;

    protected DefaultTestStepRunner(TestStepProcessor<TestStep> testStepProcessor,
                                    TestStepsRegistry testStepsRegistry,
                                    TestStepFactory testStepFactory,
                                    TestScenarioState testScenarioState) {
        this.testStepProcessor = testStepProcessor;
        this.testStepsRegistry = testStepsRegistry;
        this.testStepFactory = testStepFactory;
        this.testScenarioState = testScenarioState;
    }

    @Override
    public void run(String testStepName) {
        run(testStepName, Collections.emptyMap());
    }

    @Override
    public void run(String testStepName, Map<String, String> parameters) {
        Optional<Map<String, Object>> rawTestStep = testStepsRegistry.getByName(testStepName);
        if (rawTestStep.isPresent()) {
            TestStep testStep = testStepFactory.create(testStepName, rawTestStep.get(), parameters);

            testScenarioState.set(testStep.getId(), testStep);

            testStepProcessor.process(testStep);
        } else {
            throw new TestStepProcessingException(
                    String.format("Test step '%s' is not found in any of registered test data files from: %s",
                            testStepName, testStepsRegistry.getTestStepsRootPath()));
        }
    }

    public static class Builder {

        private TestStepProcessor<TestStep> testStepProcessor;
        private TestStepReader testStepReader;
        private TestScenarioState testScenarioState;
        private Collection<Path> testStepPaths;
        private TestStepFactory testStepFactory;

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withTestStepProcessor(TestStepProcessor<TestStep> testStepProcessor) {
            this.testStepProcessor = testStepProcessor;
            return this;
        }

        public Builder withTestStepFactory(TestStepFactory testStepFactory) {
            this.testStepFactory = testStepFactory;
            return this;
        }

        public Builder withTestStepReader(TestStepReader testStepReader, Collection<Path> testStepPaths) {
            this.testStepReader = testStepReader;
            this.testStepPaths = testStepPaths;
            return this;
        }

        public TestStepRunner build() {
            Objects.requireNonNull(testStepReader, "Test Step Reader is mandatory");
            Objects.requireNonNull(testStepProcessor, "Test Step processor is mandatory");

            TestStepsRegistry testStepsRegistry = new TestStepsRegistry(TestStepFileExtension.STS, testStepReader);
            testStepsRegistry.registerFoundTestStepsInPath(testStepPaths);

            return new DefaultTestStepRunner(testStepProcessor, testStepsRegistry, testStepFactory, testScenarioState);
        }
    }
}
