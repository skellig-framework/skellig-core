package org.skellig.teststep.processing.processor;

import org.skellig.teststep.processing.model.TestStep;

public interface TestStepProcessor<T extends TestStep> {

    void process(T testStep);

    Class<T> getTestStepClass();
}
