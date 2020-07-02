package org.skellig.teststep.processor.tcp.model;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Map;
import java.util.Optional;

public class TcpTestStep extends TestStep {

    private String sendTo;
    private String readFrom;

    protected TcpTestStep(String id, String name, Map<String, Object> variables, Object testData,
                          ValidationDetails validationDetails, String sendTo, String readFrom) {
        super(id, name, variables, testData, validationDetails);
        this.sendTo = sendTo;
        this.readFrom = readFrom;
    }

    public Optional<String> getSendTo() {
        return Optional.ofNullable(sendTo);
    }

    public Optional<String> getReadFrom() {
        return Optional.ofNullable(readFrom);
    }

    public static class Builder extends TestStep.Builder {

        private String sendTo;
        private String readFrom;

        public Builder withReadFrom(String readFrom) {
            this.readFrom = readFrom;
            return this;
        }

        public Builder withSendTo(String sendTo) {
            this.sendTo = sendTo;
            return this;
        }

        public TcpTestStep build() {
            return new TcpTestStep(id, name, variables, testData, validationDetails, sendTo, readFrom);
        }
    }
}
