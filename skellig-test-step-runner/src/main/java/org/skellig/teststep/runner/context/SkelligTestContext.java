package org.skellig.teststep.runner.context;

import org.skellig.teststep.processing.converter.DefaultTestDataConverter;
import org.skellig.teststep.processing.converter.DefaultValueConverter;
import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.DefaultTestStepFactory;
import org.skellig.teststep.processing.model.factory.TestStepFactory;
import org.skellig.teststep.processing.processor.DefaultTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.state.ThreadLocalTestScenarioState;
import org.skellig.teststep.processing.validation.DefaultTestStepResultValidator;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processing.validation.comparator.DefaultValueComparator;
import org.skellig.teststep.processing.validation.comparator.ValueComparator;
import org.skellig.teststep.processing.valueextractor.DefaultValueExtractor;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.sts.StsTestStepReader;
import org.skellig.teststep.runner.DefaultTestStepRunner;
import org.skellig.teststep.runner.TestStepRunner;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

public class SkelligTestContext {

    private TestStepValueConverter testStepValueConverter;
    private TestDataConverter testDataConverter;
    private TestScenarioState testScenarioState;

    public TestStepRunner initialize(ClassLoader classLoader, List<Path> testStepPaths) {
        TestStepReader testStepReader = createTestStepReader();
        testScenarioState = createTestScenarioState();

        TestStepValueExtractor valueExtractor = createTestStepValueExtractor();
        testStepValueConverter = createTestStepValueConverter(classLoader, valueExtractor, testScenarioState);

        testDataConverter = createTestDataConverter(classLoader);

        List<TestStepProcessorDetails> testStepProcessors = getTestStepProcessors();
        TestStepProcessor<TestStep> testStepProcessor = createTestStepProcessor(valueExtractor, testStepProcessors, testScenarioState);
        TestStepFactory testStepFactory = createTestStepFactory(testStepProcessors);

        return new DefaultTestStepRunner.Builder()
                .withTestScenarioState(testScenarioState)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepFactory(testStepFactory)
                .withTestStepReader(testStepReader, testStepPaths)
                .build();
    }

    private TestStepFactory createTestStepFactory(List<TestStepProcessorDetails> testStepProcessors) {
        DefaultTestStepFactory.Builder testStepFactoryBuilder = new DefaultTestStepFactory.Builder();
        testStepProcessors.forEach(item -> testStepFactoryBuilder.withTestStepFactory(item.getTestStepFactory()));

        return testStepFactoryBuilder
                .withKeywordsProperties(getTestStepKeywordsProperties())
                .withTestStepValueConverter(testStepValueConverter)
                .withTestDataConverter(testDataConverter)
                .build();
    }

    public final TestScenarioState getTestScenarioState() {
        Objects.requireNonNull(testScenarioState, "TestScenarioState must be initialized first. Did you forget to call 'initialize'?");
        return testScenarioState;
    }

    private TestStepProcessor<TestStep> createTestStepProcessor(TestStepValueExtractor valueExtractor,
                                                                List<TestStepProcessorDetails> additionalTestStepProcessors,
                                                                TestScenarioState testScenarioState) {

        DefaultTestStepProcessor.Builder testStepProcessorBuilder = new DefaultTestStepProcessor.Builder();
        additionalTestStepProcessors.forEach(item -> testStepProcessorBuilder.withTestStepProcessor(item.getTestStepProcessor()));
        return testStepProcessorBuilder
                .withTestScenarioState(testScenarioState)
                .withValidator(createTestStepValidator(valueExtractor))
                .build();
    }

    private TestStepResultValidator createTestStepValidator(TestStepValueExtractor valueExtractor) {
        DefaultValueComparator.Builder valueComparatorBuilder = new DefaultValueComparator.Builder();
        getAdditionalValueComparators().forEach(valueComparatorBuilder::withValueComparator);

        DefaultTestStepResultValidator.Builder validatorBuilder = new DefaultTestStepResultValidator.Builder();
        return validatorBuilder
                .withValueExtractor(valueExtractor)
                .withValueComparator(valueComparatorBuilder.build())
                .build();
    }

    private TestStepValueConverter createTestStepValueConverter(ClassLoader classLoader, TestStepValueExtractor valueExtractor,
                                                                TestScenarioState testScenarioState) {
        DefaultValueConverter.Builder valueConverterBuilder = new DefaultValueConverter.Builder();
        getAdditionalTestStepValueConverters().forEach(valueConverterBuilder::withValueConverter);

        return valueConverterBuilder
                .withClassLoader(classLoader)
                .withGetPropertyFunction(getPropertyExtractorFunction())
                .withTestScenarioState(testScenarioState)
                .withTestStepValueExtractor(valueExtractor)
                .build();
    }

    private TestDataConverter createTestDataConverter(ClassLoader classLoader) {
        final DefaultTestDataConverter.Builder builder = new DefaultTestDataConverter.Builder();
        getAdditionalTestDataConverters().forEach(builder::withTestDataConverter);

        return builder.withClassLoader(classLoader).build();
    }

    private TestStepValueExtractor createTestStepValueExtractor() {
        DefaultValueExtractor.Builder valueExtractorBuilder = new DefaultValueExtractor.Builder();
        getAdditionalTestStepValueExtractors().forEach(valueExtractorBuilder::withValueExtractor);
        return valueExtractorBuilder.build();
    }

    protected TestStepReader createTestStepReader() {
        return new StsTestStepReader();
    }

    protected TestScenarioState createTestScenarioState() {
        return new ThreadLocalTestScenarioState();
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

    protected List<TestDataConverter> getAdditionalTestDataConverters() {
        return Collections.emptyList();
    }

    protected List<TestStepProcessorDetails> getTestStepProcessors() {
        return Collections.emptyList();
    }

    protected Function<String, String> getPropertyExtractorFunction() {
        return null;
    }

    protected Properties getTestStepKeywordsProperties() {
        return null;
    }

    protected TestStepFactory createTestStepFactoryFrom(TestStepFactoryCreationDelegate delegate) {
        Objects.requireNonNull(testStepValueConverter, "TestStepValueConverter must be initialized first. Did you forget to call 'initialize'?");
        Objects.requireNonNull(testDataConverter, "TestDataConverter must be initialized first. Did you forget to call 'initialize'?");

        return delegate.create(getTestStepKeywordsProperties(), testStepValueConverter, testDataConverter);
    }

    protected static final class TestStepProcessorDetails {

        private TestStepProcessor testStepProcessor;
        private TestStepFactory testStepFactory;

        protected TestStepProcessorDetails(TestStepProcessor testStepProcessor, TestStepFactory testStepFactory) {
            this.testStepProcessor = testStepProcessor;
            this.testStepFactory = testStepFactory;
        }

        protected TestStepProcessor getTestStepProcessor() {
            return testStepProcessor;
        }

        protected TestStepFactory getTestStepFactory() {
            return testStepFactory;
        }

    }

    protected interface TestStepFactoryCreationDelegate {
        TestStepFactory create(Properties keywordsProperties, TestStepValueConverter testStepValueConverter,
                               TestDataConverter testDataConverter);
    }
}
