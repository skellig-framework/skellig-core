package org.skellig.test.processing;

import org.skellig.test.processing.processor.DefaultTestStepProcessor;
import org.skellig.test.processing.processor.TestStepProcessor;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.sts.StsTestStepReader;

import java.util.Properties;

public class SkelligTestContext {

    protected TestStepReader testStepReader;
    protected TestStepProcessor testStepProcessor;

    public SkelligTestContext() {
    }

    public void initialize() {
        if (testStepReader == null) {
            testStepReader = new StsTestStepReader.Builder()
                    .withDefaultTestStepFactory(getTestStepKeywordsProperties())
                    .build();
        }

        if(testStepProcessor == null){
            testStepProcessor = new DefaultTestStepProcessor();
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
