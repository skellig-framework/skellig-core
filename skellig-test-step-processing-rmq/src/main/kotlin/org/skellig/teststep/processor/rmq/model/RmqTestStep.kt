package org.skellig.teststep.processor.rmq.model;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Map;
import java.util.Optional;

public class RmqTestStep extends TestStep {

    private String sendTo;
    private String receiveFrom;
    private String respondTo;
    private String routingKey;

    protected RmqTestStep(String id, String name, TestStepExecutionType execution, int timeout, int delay,
                          Map<String, Object> variables, Object testData, ValidationDetails validationDetails,
                          String sendTo, String receiveFrom, String respondTo, String routingKey) {
        super(id, name, execution, timeout, delay, variables, testData, validationDetails);
        this.sendTo = sendTo;
        this.receiveFrom = receiveFrom;
        this.respondTo = respondTo;
        this.routingKey = routingKey;
    }

    public Optional<String> getSendTo() {
        return Optional.ofNullable(sendTo);
    }

    public Optional<String> getReceiveFrom() {
        return Optional.ofNullable(receiveFrom);
    }

    public Optional<String> getRespondTo() {
        return Optional.ofNullable(respondTo);
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public static class Builder extends TestStep.Builder {

        private String sendTo;
        private String receiveFrom;
        private String respondTo;
        private String routingKey;

        public Builder withSendTo(String sendTo) {
            this.sendTo = sendTo;
            return this;
        }

        public Builder withReceiveFrom(String receiveFrom) {
            this.receiveFrom = receiveFrom;
            return this;
        }

        public Builder withRespondTo(String respondTo) {
            this.respondTo = respondTo;
            return this;
        }

        public Builder withRoutingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        @Override
        public RmqTestStep build() {
            return new RmqTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails,
                    sendTo, receiveFrom, respondTo, routingKey);
        }
    }
}
