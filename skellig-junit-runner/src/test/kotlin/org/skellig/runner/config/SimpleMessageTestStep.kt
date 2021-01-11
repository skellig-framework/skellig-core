package org.skellig.runner.config;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Map;

public class SimpleMessageTestStep extends TestStep {

    private String receiver;
    private String receiveFrom;

    protected SimpleMessageTestStep(String id, String name, TestStepExecutionType execution, int timeout, int delay,
                                    Map<String, Object> variables, Object testData, ValidationDetails validationDetails,
                                    String receiver, String receiveFrom) {
        super(id, name, execution, timeout, delay, variables, testData, validationDetails);
        this.receiver = receiver;
        this.receiveFrom = receiveFrom;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getReceiveFrom() {
        return receiveFrom;
    }

    public static class Builder extends TestStep.Builder {

        private String receiver;
        private String receiveFrom;

        public Builder withReceiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder withReceiveFrom(String receiveFrom) {
            this.receiveFrom = receiveFrom;
            return this;
        }

        @Override
        public TestStep build() {
            return new SimpleMessageTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails, receiver, receiveFrom);
        }
    }
}
