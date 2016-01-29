package org.skellig.teststep.processing.processor;

import org.skellig.teststep.reader.model.TestStep;

public interface TestStepProcessor {

    void process(TestStep testStep);

    Class<? extends TestStep> getTestStepClass();
}
