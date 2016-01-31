package org.skellig.teststep.processing;

import org.skellig.teststep.processing.converter.DefaultValueConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.HttpTestStep;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.DefaultTestStepFactory;
import org.skellig.teststep.processing.model.factory.TestStepFactory;
import org.skellig.teststep.processing.processor.DefaultTestStepProcessor;
import org.skellig.teststep.processing.processor.HttpTestStepProcessor;
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
import org.skellig.teststep.reader.sts.StsTestStepReader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

public class SkelligTestContext {

    public TestStepRunner initialize(ClassLoader classLoader, List<Path> testStepPaths) {
        TestStepReader testStepReader = createTestStepReader();
        TestScenarioState testScenarioState = createTestScenarioState();

        TestStepValueExtractor valueExtractor = createTestStepValueExtractor();
        TestStepValueConverter testStepValueConverter = createTestStepValueConverter(classLoader, valueExtractor, testScenarioState);
        List<TestStepProcessorDetails> additionalTestStepProcessors = getAdditionalTestStepProcessors();

        TestStepProcessor<TestStep> testStepProcessor = initializeTestStepProcessor(valueExtractor, additionalTestStepProcessors, testScenarioState);

        DefaultTestStepFactory.Builder testStepFactoryBuilder = new DefaultTestStepFactory.Builder();
        additionalTestStepProcessors.forEach(item -> testStepFactoryBuilder.withTestStepFactory(item.getTestStepFactory()));

        TestStepFactory testStepFactory =
                testStepFactoryBuilder
                        .withKeywordsProperties(getTestStepKeywordsProperties())
                        .withTestStepValueConverter(testStepValueConverter)
                        .build();

        return new DefaultTestStepRunner.Builder()
                .withTestScenarioState(testScenarioState)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepFactory(testStepFactory)
                .withTestStepReader(testStepReader, testStepPaths)
                .build();
    }

    private TestStepProcessor<TestStep> initializeTestStepProcessor(TestStepValueExtractor valueExtractor,
                                                                    List<TestStepProcessorDetails> additionalTestStepProcessors,
                                                                    TestScenarioState testScenarioState) {
        DefaultValueComparator.Builder valueComparatorBuilder = new DefaultValueComparator.Builder();
        getAdditionalValueComparators().forEach(valueComparatorBuilder::withValueComparator);

        DefaultTestStepResultValidator.Builder validatorBuilder = new DefaultTestStepResultValidator.Builder();
        TestStepResultValidator validator =
                validatorBuilder
                        .withValueExtractor(valueExtractor)
                        .withValueComparator(valueComparatorBuilder.build())
                        .build();

        //TODO: find a way to provide http services and urls
        TestStepProcessor<HttpTestStep> testStepProcessor = new HttpTestStepProcessor.Builder()
                .withHttpService("", "")
                .withTestScenarioState(testScenarioState)
                .build();

        DefaultTestStepProcessor.Builder testStepProcessorBuilder = new DefaultTestStepProcessor.Builder();
        additionalTestStepProcessors.forEach(item -> testStepProcessorBuilder.withTestStepProcessor(item.getTestStepProcessor()));
        return testStepProcessorBuilder
                .withTestScenarioState(testScenarioState)
                .withValidator(validator)
                .withTestStepProcessor(testStepProcessor)
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

    protected List<TestStepProcessorDetails> getAdditionalTestStepProcessors() {
        return Collections.emptyList();
    }

    protected Function<String, String> getPropertyExtractorFunction() {
        return null;
    }

    protected Properties getTestStepKeywordsProperties() {
        return null;
    }

    public static final class TestStepProcessorDetails {
        private TestStepProcessor<? super TestStep> testStepProcessor;
        private TestStepFactory testStepFactory;

        public TestStepProcessorDetails(TestStepProcessor<? super TestStep> testStepProcessor, TestStepFactory testStepFactory) {
            this.testStepProcessor = testStepProcessor;
            this.testStepFactory = testStepFactory;
        }

        public TestStepProcessor<? super TestStep> getTestStepProcessor() {
            return testStepProcessor;
        }

        public TestStepFactory getTestStepFactory() {
            return testStepFactory;
        }
    }
}
