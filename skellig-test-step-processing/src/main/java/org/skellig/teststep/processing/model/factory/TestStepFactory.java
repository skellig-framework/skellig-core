package org.skellig.teststep.processing.model.factory;

import org.skellig.teststep.processing.model.TestStep;

import java.util.Map;

public interface TestStepFactory {

    TestStep create(String testStepName, Map<String, Object> rawTestStep, Map<String, String> parameters);

    boolean isConstructableFrom(Map<String, Object> rawTestStep);
}
