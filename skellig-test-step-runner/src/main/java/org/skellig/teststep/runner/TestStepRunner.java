package org.skellig.teststep.runner;

import org.skellig.teststep.processing.processor.TestStepProcessor;

import java.util.Map;

public interface TestStepRunner {

    TestStepProcessor.TestStepRunResult run(String testStepName);

    TestStepProcessor.TestStepRunResult run(String testStepName, Map<String, String> parameters);
}
