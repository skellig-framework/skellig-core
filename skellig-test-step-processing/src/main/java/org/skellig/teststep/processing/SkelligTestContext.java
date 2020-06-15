package org.skellig.teststep.processing;

import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.processor.DefaultTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.state.ThreadLocalTestScenarioState;
import org.skellig.teststep.processing.valueextractor.TestStepValueExtractor;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.model.factory.TestStepFactory;
import org.skellig.teststep.reader.sts.StsTestStepReader;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class SkelligTestContext {

    private TestStepReader testStepReader;
    private TestStepProcessor testStepProcessor;
    private TestScenarioState testScenarioState;

    public SkelligTestContext() {
    }

    public void initialize(ClassLoader classLoader) {
        List<TestStepProcessorDetails> additionalTestStepProcessors = getAdditionalTestStepProcessors();

        if (testScenarioState == null) {
            testScenarioState = new ThreadLocalTestScenarioState();
        }

        if (testStepReader == null) {
            StsTestStepReader.Builder builder = new StsTestStepReader.Builder();
            additionalTestStepProcessors.forEach(item -> builder.withTestStepFactory(item.getTestStepFactory()));
            testStepReader = builder.build(getTestStepKeywordsProperties());
        }

        if (testStepProcessor == null) {
            DefaultTestStepProcessor.Builder builder = new DefaultTestStepProcessor.Builder();
            getAdditionalTestStepValueExtractors().forEach(builder::withValueOfStateExtractor);
            getAdditionalTestStepValueConverters().forEach(builder::withValueConverter);
            additionalTestStepProcessors.forEach(item -> builder.withTestStepProcessors(item.getTestStepProcessor()));

            testStepProcessor = builder
                    .withClassLoader(classLoader)
                    .withTestScenarioState(testScenarioState)
                    .build();
        }
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
