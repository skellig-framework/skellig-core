package org.skellig.teststep.processor.ibmmq;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails;
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IbmMqTestStepProcessor extends BaseTestStepProcessor<IbmMqTestStep> {

    private Map<String, IbmMqChannel> ibmMqChannels;

    protected IbmMqTestStepProcessor(TestScenarioState testScenarioState, TestStepResultValidator validator,
                                     TestStepResultConverter testStepResultConverter,
                                     Map<String, IbmMqChannel> ibmMqChannels) {
        super(testScenarioState, validator, testStepResultConverter);
        this.ibmMqChannels = ibmMqChannels;
    }

    @Override
    protected Object processTestStep(IbmMqTestStep testStep) {
        Object response = null;
        Optional<String> sendTo = testStep.getSendTo();
        Optional<String> receiveFrom = testStep.getReceiveFrom();
        Optional<String> respondTo = testStep.getRespondTo();

        sendTo.ifPresent(channelId -> send(testStep.getTestData(), channelId));

        if (receiveFrom.isPresent()) {
            IbmMqChannel ibmMqChannel = ibmMqChannels.get(receiveFrom.get());
            response = ibmMqChannel.read(testStep.getTimeout());

            validate(testStep, response);
            respondTo.ifPresent(channel -> send(testStep.getTestData(), channel));
        }
        return response;
    }

    private void send(Object testData, String channelId) {
        if (ibmMqChannels.containsKey(channelId)) {
            IbmMqChannel ibmMqChannel = ibmMqChannels.get(channelId);
            ibmMqChannel.send(testData);
        } else {
            throw new TestStepProcessingException(String.format("Channel '%s' was not registered " +
                    "in IBMMQ Test Step Processor", channelId));
        }
    }

    @Override
    public Class<IbmMqTestStep> getTestStepClass() {
        return IbmMqTestStep.class;
    }

    public static class Builder extends BaseTestStepProcessor.Builder<IbmMqTestStep> {

        private Map<String, IbmMqChannel> ibmMqChannels;

        public Builder() {
            ibmMqChannels = new HashMap<>();
        }

        public Builder withIbmMqChannel(IbmMqQueueDetails mqQueueDetails) {
            this.ibmMqChannels.putIfAbsent(mqQueueDetails.getChannelId(), new IbmMqChannel(mqQueueDetails));
            return this;
        }

        public Builder withIbmMqChannels(Config config) {
            return this;
        }

        @Override
        public TestStepProcessor<IbmMqTestStep> build() {
            return new IbmMqTestStepProcessor(testScenarioState, validator, testStepResultConverter, ibmMqChannels);
        }
    }
}
