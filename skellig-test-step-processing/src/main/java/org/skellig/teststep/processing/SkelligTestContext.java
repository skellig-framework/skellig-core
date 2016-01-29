package org.skellig.teststep.processing;

import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.processor.DefaultTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.runner.DefaultTestStepRunner;
import org.skellig.teststep.processing.runner.TestStepRunner;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.state.ThreadLocalTestScenarioState;
import org.skellig.teststep.processing.validation.DefaultTestStepResultValidator;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator;
import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.model.factory.TestStepFactory;
import org.skellig.teststep.reader.sts.StsTestStepReader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class SkelligTestContext {

    private TestStepReader testStepReader;
    private TestStepProcessor testStepProcessor;
    private TestScenarioState testScenarioState;
    private TestStepRunner testStepRunner;

    public SkelligTestContext() {
    }

    public void initialize(ClassLoader classLoader, List<Path> testStepPaths) {
        List<TestStepProcessorDetails> additionalTestStepProcessors = getAdditionalTestStepProcessors();

        if (testScenarioState == null) {
            testScenarioState = new ThreadLocalTestScenarioState();
        }

        if (testStepReader == null) {
            StsTestStepReader.Builder builder = new StsTestStepReader.Builder();
            additionalTestStepProcessors.forEach(item -> builder.withTestStepFactory(item.getTestStepFactory()));
            testStepReader = builder.build(getTestStepKeywordsProperties());
        }

        List<TestStepValueExtractor> additionalTestStepValueExtractors = getAdditionalTestStepValueExtractors();
        DefaultValueExtractor.Builder valueExtractorBuilder = new DefaultValueExtractor.Builder();
        additionalTestStepValueExtractors.forEach(valueExtractorBuilder::withValueExtractor);
        TestStepValueExtractor valueExtractor = valueExtractorBuilder.build();

        if (testStepProcessor == null) {

            DefaultValueComparator.Builder valueComparatorBuilder = new DefaultValueComparator.Builder();
            getAdditionalValueComparators().forEach(valueComparatorBuilder::withValueComparator);

            DefaultTestStepResultValidator.Builder validatorBuilder = new DefaultTestStepResultValidator.Builder();
            TestStepResultValidator validator =
                    validatorBuilder
                            .withValueExtractor(valueExtractor)
                            .withValueComparator(valueComparatorBuilder.build())
                            .build();

            DefaultTestStepProcessor.Builder testStepProcessorBuilder = new DefaultTestStepProcessor.Builder();
            additionalTestStepProcessors.forEach(item -> testStepProcessorBuilder.withTestStepProcessor(item.getTestStepProcessor()));
            testStepProcessor =
                    testStepProcessorBuilder
                            .withTestScenarioState(testScenarioState)
                            .withValidator(validator)
                            .build();
        }

        if (testStepRunner == null) {
            DefaultTestStepRunner.Builder testStepRunnerBuilder = new DefaultTestStepRunner.Builder();
            additionalTestStepValueExtractors.forEach(testStepRunnerBuilder::withValueExtractor);
            getAdditionalTestStepValueConverters().forEach(testStepRunnerBuilder::withValueConverter);

            testStepRunner =
                    testStepRunnerBuilder
                            .withClassLoader(classLoader)
                            .withTestScenarioState(getTestScenarioState())
                            .withTestStepProcessor(getTestStepProcessor())
                            .withValueExtractor(valueExtractor)
                            .withTestStepReader(getTestStepReader(), testStepPaths)
                            .build();
        }
    }

    public final TestStepRunner getTestStepRunner() {
        return testStepRunner;
    }

    public final TestStepReader getTestStepReader() {
        return testStepReader;
    }

    public final TestStepProcessor getTestStepProcessor() {
        return testStepProcessor;
    }

    public final TestScenarioState getTestScenarioState() {
        return testScenarioState;
    }

    protected List<TestStepValueExtractor> getAdditionalTestStepValueExtractors() {
        return Collections.emptyList();
    }

    protected List<ValueComparator> getAdditionalValueComparators() {
        return Collections.emptyList();
    }

    protected List<TestStepValueConverter> getAdditionalTestStepValueConverters() {
        return Collections.emptyList();
    }

    protected List<TestStepProcessorDetails> getAdditionalTestStepProcessors() {
        return Collections.emptyList();
    }

    protected Properties getTestStepKeywordsProperties() {
        return null;
    }

    public static final class TestStepProcessorDetails {
        private TestStepProcessor testStepProcessor;
        private TestStepFactory testStepFactory;

        public TestStepProcessorDetails(TestStepProcessor testStepProcessor, TestStepFactory testStepFactory) {
            this.testStepProcessor = testStepProcessor;
            this.testStepFactory = testStepFactory;
        }

        public TestStepProcessor getTestStepProcessor() {
            return testStepProcessor;
        }

        public TestStepFactory getTestStepFactory() {
            return testStepFactory;
        }
    }
}
