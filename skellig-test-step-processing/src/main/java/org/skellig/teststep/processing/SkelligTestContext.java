package org.skellig.teststep.processing;

import org.skellig.teststep.processing.processor.DefaultTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.state.ThreadLocalTestScenarioState;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.sts.StsTestStepReader;

import java.util.Properties;

public class SkelligTestContext {

    protected TestStepReader testStepReader;
    protected TestStepProcessor testStepProcessor;
    protected TestScenarioState testScenarioState;

    public SkelligTestContext() {
    }

    public void initialize(ClassLoader classLoader) {
        if(testScenarioState == null){
            testScenarioState = new ThreadLocalTestScenarioState();
        }

        if (testStepReader == null) {
            testStepReader = new StsTestStepReader.Builder()
                    .withDefaultTestStepFactory(getTestStepKeywordsProperties())
                    .build();
        }

        if(testStepProcessor == null){
            testStepProcessor = new DefaultTestStepProcessor.Builder()
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

    protected Properties getTestStepKeywordsProperties() {
        return null;
    }
}
