package org.skellig.teststep.processor.tcp.model;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Map;
import java.util.Optional;

public class TcpTestStep extends TestStep {

    private String sendTo;
    private String receiveFrom;
    private String respondTo;
    private int readBufferSize;

    protected TcpTestStep(String id, String name, TestStepExecutionType execution, int timeout, int delay,
                          Map<String, Object> variables, Object testData, ValidationDetails validationDetails,
                          String sendTo, String receiveFrom, String respondTo, int readBufferSize) {
        super(id, name, execution, timeout, delay, variables, testData, validationDetails);
        this.sendTo = sendTo;
        this.respondTo = respondTo;
        this.receiveFrom = receiveFrom;
        this.readBufferSize = readBufferSize;
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

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public static class Builder extends TestStep.Builder {

        private String sendTo;
        private String receiveFrom;
        private String respondTo;
        private int readBufferSize = 1024 * 1024;

        public Builder withReceiveFrom(String receiveFrom) {
            this.receiveFrom = receiveFrom;
            return this;
        }

        public Builder withRespondTo(String respondTo) {
            this.respondTo = respondTo;
            return this;
        }

        public Builder withSendTo(String sendTo) {
            this.sendTo = sendTo;
            return this;
        }

        public Builder withReadBufferSize(int readBufferSize) {
            this.readBufferSize = readBufferSize;
            return this;
        }

        public TcpTestStep build() {
            return new TcpTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails,
                    sendTo, receiveFrom, respondTo, readBufferSize);
        }
    }
}
