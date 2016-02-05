package org.skellig.teststep.runner;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.TestStepFactory;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.runner.exception.TestStepRegistryException;
import org.skellig.teststep.runner.model.TestStepFileExtension;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultTestStepRunner implements TestStepRunner {

    private TestStepProcessor<TestStep> testStepProcessor;
    private TestStepsRegistry testStepsRegistry;
    private ClassTestStepsRegistry classTestStepsRegistry;
    private TestStepFactory testStepFactory;
    private TestStepDefMethodRunner testStepDefMethodRunner;

    protected DefaultTestStepRunner(TestStepProcessor<TestStep> testStepProcessor,
                                    TestStepsRegistry testStepsRegistry,
                                    ClassTestStepsRegistry classTestStepsRegistry,
                                    TestStepFactory testStepFactory) {
        this.testStepProcessor = testStepProcessor;
        this.testStepsRegistry = testStepsRegistry;
        this.classTestStepsRegistry = classTestStepsRegistry;
        this.testStepFactory = testStepFactory;

        testStepDefMethodRunner = new TestStepDefMethodRunner();
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

            testStepProcessor.process(testStep);
        } else {
            Optional<ClassTestStepsRegistry.TestStepDefDetails> testStep = classTestStepsRegistry.getTestStep(testStepName);
            if (testStep.isPresent()) {
                testStepDefMethodRunner.invoke(testStepName, testStep.get(), parameters);
            } else {
                throw new TestStepProcessingException(
                        String.format("Test step '%s' is not found in any of registered test data files from: %s",
                                testStepName, testStepsRegistry.getTestStepsRootPath()));
            }
        }
    }

    public static class Builder {

        private TestStepProcessor<TestStep> testStepProcessor;
        private TestStepReader testStepReader;
        private ClassLoader classLoader;
        private Collection<String> testStepPaths;
        private TestStepFactory testStepFactory;

        public Builder withTestStepProcessor(TestStepProcessor<TestStep> testStepProcessor) {
            this.testStepProcessor = testStepProcessor;
            return this;
        }

        public Builder withTestStepFactory(TestStepFactory testStepFactory) {
            this.testStepFactory = testStepFactory;
            return this;
        }

        public Builder withTestStepReader(TestStepReader testStepReader, ClassLoader classLoader, Collection<String> testStepPaths) {
            this.testStepReader = testStepReader;
            this.classLoader = classLoader;
            this.testStepPaths = testStepPaths;
            return this;
        }

        public TestStepRunner build() {
            Objects.requireNonNull(testStepReader, "Test Step Reader is mandatory");
            Objects.requireNonNull(testStepProcessor, "Test Step processor is mandatory");

            Collection<Path> testStepPaths = extractTestStepPaths();
            Collection<String> testStepClassPaths = extractTestStepPackages();

            TestStepsRegistry testStepsRegistry = new TestStepsRegistry(TestStepFileExtension.STS, testStepReader);
            testStepsRegistry.registerFoundTestStepsInPath(testStepPaths);

            ClassTestStepsRegistry classTestStepsRegistry = new ClassTestStepsRegistry();
            classTestStepsRegistry.registerFoundTestStepInClasses(testStepClassPaths, classLoader);

            return new DefaultTestStepRunner(testStepProcessor, testStepsRegistry, classTestStepsRegistry, testStepFactory);
        }

        private Collection<String> extractTestStepPackages() {
            return this.testStepPaths.stream()
                            .filter(path -> !path.contains("/"))
                            .collect(Collectors.toSet());
        }

        private Collection<Path> extractTestStepPaths() {
            return this.testStepPaths.stream()
                            .filter(path -> !path.contains("."))
                            .map(path -> {
                                try {
                                    URL resource = classLoader.getResource(path);
                                    return Paths.get(resource.toURI());
                                } catch (URISyntaxException e) {
                                    throw new TestStepRegistryException(e.getMessage(), e);
                                }
                            })
                            .collect(Collectors.toList());
        }
    }
}
