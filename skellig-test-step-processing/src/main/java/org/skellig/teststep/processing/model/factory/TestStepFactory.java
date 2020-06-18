package org.skellig.teststep.processing.model.factory;

import org.skellig.teststep.processing.model.TestStep;

import java.util.Map;

public interface TestStepFactory {

    TestStep create(Map<String, Object> rawTestStep);

    boolean isConstructableFrom(Map<String, Object> rawTestStep);
}
