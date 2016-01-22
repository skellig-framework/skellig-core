package org.skellig.teststep.processing.processor;

import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;

public interface TestStepProcessor {

    void process(TestStep testStep, Map<String, String> parameters);

    Class<? extends TestStep> getTestStepClass();
}
