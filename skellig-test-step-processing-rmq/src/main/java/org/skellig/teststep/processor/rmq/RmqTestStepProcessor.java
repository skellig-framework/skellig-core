package org.skellig.teststep.processor.rmq;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.rmq.model.RmqDetails;
import org.skellig.teststep.processor.rmq.model.RmqTestStep;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RmqTestStepProcessor extends BaseTestStepProcessor<RmqTestStep> {

    private Map<String, RmqChannel> rmqChannels;

    protected RmqTestStepProcessor(Map<String, RmqChannel> rmqChannels,
                                   TestScenarioState testScenarioState,
                                   TestStepResultValidator validator,
                                   TestStepResultConverter testStepResultConverter) {
        super(testScenarioState, validator, testStepResultConverter);
        this.rmqChannels = rmqChannels;
    }

    @Override
    protected Object processTestStep(RmqTestStep testStep) {
        Object response = null;
        Optional<String> sendTo = testStep.getSendTo();
        Optional<String> receiveFrom = testStep.getReceiveFrom();
        String routingKey = testStep.getRoutingKey();

        sendTo.ifPresent(channelId -> send(testStep.getTestData(), channelId, routingKey));

        if (receiveFrom.isPresent()) {
            RmqChannel channel = rmqChannels.get(receiveFrom.get());
            Optional<String> respondTo = testStep.getRespondTo();
            Object responseTestData = testStep.getTestData();
            response = channel.read(respondTo.isPresent() ? null : responseTestData, testStep.getTimeout());

            if (respondTo.isPresent() && response != null) {
                send(responseTestData, respondTo.get(), routingKey);
            }
        }
        return response;
    }

    private void send(Object testData, String channelId, String routingKey) {
        RmqChannel channel = rmqChannels.get(channelId);
        channel.send(testData, routingKey);
    }

    @Override
    public void close() {
        rmqChannels.values().forEach(RmqChannel::close);
    }

    @Override
    public Class<RmqTestStep> getTestStepClass() {
        return RmqTestStep.class;
    }

    public static class Builder extends BaseTestStepProcessor.Builder<RmqTestStep> {

        private Map<String, RmqChannel> rmqChannels;
        private RmqDetailsConfigReader rmqDetailsConfigReader;

        public Builder() {
            rmqDetailsConfigReader = new RmqDetailsConfigReader();
            rmqChannels = new HashMap<>();
        }

        public Builder withRmqChannel(RmqDetails rmqDetails) {
            this.rmqChannels.putIfAbsent(rmqDetails.getChannelId(), new RmqChannel(rmqDetails));
            return this;
        }

        public Builder withRmqChannels(Config config) {
            rmqDetailsConfigReader.read(config).forEach(this::withRmqChannel);
            return this;
        }

        @Override
        public TestStepProcessor<RmqTestStep> build() {
            return new RmqTestStepProcessor(rmqChannels, testScenarioState, validator, testStepResultConverter);
        }
    }
}
