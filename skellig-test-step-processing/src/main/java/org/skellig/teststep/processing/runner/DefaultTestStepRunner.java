package org.skellig.teststep.processing.runner;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.model.TestStep;
import org.skellig.teststep.reader.model.TestStepFileExtension;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DefaultTestStepRunner implements TestStepRunner {

    private TestStepProcessor testStepProcessor;
    private TestStepsRegistry testStepsRegistry;

    protected DefaultTestStepRunner(TestStepProcessor testStepProcessor, TestStepsRegistry testStepsRegistry) {
        this.testStepProcessor = testStepProcessor;
        this.testStepsRegistry = testStepsRegistry;
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
            testStepProcessor.process(testStep.get(), additionalParameters);
        } else {
            throw new TestStepProcessingException(
                    String.format("Test step '%s' is not found in any of registered test data files from: %s",
                            testStepName, testStepsRegistry.getTestStepsRootPath()));
        }
    }


    public static class Builder {
        private TestStepProcessor testStepProcessor;
        private TestStepReader testStepReader;
        private Collection<Path> testStepPaths;

        public Builder withTestStepProcessor(TestStepProcessor testStepProcessor) {
            this.testStepProcessor = testStepProcessor;
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

            return new DefaultTestStepRunner(testStepProcessor, testStepsRegistry);
        }
    }
}
