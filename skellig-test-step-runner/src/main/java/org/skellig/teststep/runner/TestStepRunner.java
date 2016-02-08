package org.skellig.teststep.runner;

import java.util.Map;

public interface TestStepRunner {

    String run(String testStepName);

    String run(String testStepName, Map<String, String> parameters);
}
