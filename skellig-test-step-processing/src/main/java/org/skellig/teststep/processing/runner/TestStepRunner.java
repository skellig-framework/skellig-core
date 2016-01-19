package org.skellig.teststep.processing.runner;

import java.util.Map;

public interface TestStepRunner {

    void run(String testStepName);

    void run(String testStepName, Map<String, String> parameters);
}
