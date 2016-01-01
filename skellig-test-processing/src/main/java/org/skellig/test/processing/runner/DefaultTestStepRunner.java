package org.skellig.test.processing.runner;

import org.skellig.test.processing.processor.DefaultTestStepProcessor;
import org.skellig.test.processing.processor.TestStepProcessor;

import java.util.Collections;
import java.util.Map;

public class DefaultTestStepRunner implements TestStepRunner {

    private TestStepProcessor testStepProcessor;

    protected DefaultTestStepRunner(TestStepProcessor testStepProcessor) {
        this.testStepProcessor = testStepProcessor;
    }

    @Override
    public void run(String testStepName, String... testDataFilePaths) {
        run(testStepName, Collections.emptyMap(), testDataFilePaths);
    }

    @Override
    public void run(String testStepName, Map<String, String> parameters, String... testDataFilePaths) {
        testStepProcessor.process(null);
    }

    public static class Builder {
        private TestStepProcessor testStepProcessor;

        public Builder withTestStepProcessor(TestStepProcessor testStepProcessor) {
            this.testStepProcessor = testStepProcessor;
            return this;
        }

        public TestStepRunner build() {
            if (testStepProcessor == null) {
                withTestStepProcessor(new DefaultTestStepProcessor());
            }
            return new DefaultTestStepRunner(testStepProcessor);
        }
    }
}
