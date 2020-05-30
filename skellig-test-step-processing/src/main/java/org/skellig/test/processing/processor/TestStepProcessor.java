package org.skellig.test.processing.processor;

import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;

public interface TestStepProcessor {

    void process(TestStep testStep, Map<String, String> parameters);
}
