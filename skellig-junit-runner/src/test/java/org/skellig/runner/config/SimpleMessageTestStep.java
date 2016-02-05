package org.skellig.runner.config;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Map;

public class SimpleMessageTestStep extends TestStep {

    private String receiver;

    protected SimpleMessageTestStep(String id, String name, Map<String, Object> variables,
                                    Object testData, ValidationDetails validationDetails, String receiver) {
        super(id, name, variables, testData, validationDetails);
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

    public static class Builder extends TestStep.Builder {

        private String receiver;

        public Builder withReceiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        @Override
        public TestStep build() {
            return new SimpleMessageTestStep(id, name, variables, testData, validationDetails, receiver);
        }
    }
}
