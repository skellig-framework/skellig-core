package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;

public interface TestStepFactory {

    TestStep create(Map<String, Object> rawTestStep);

    boolean isConstructableFrom(Map<String, Object> rawTestStep);
}
