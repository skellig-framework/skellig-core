package org.skellig.teststep.processor.ibmmq;

import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep;

import java.util.Map;
import java.util.Optional;

public class IbmMqTestStepProcessor extends BaseTestStepProcessor<IbmMqTestStep> {

    private Map<String, IbmMqChannel> ibmMqChannels;

    protected IbmMqTestStepProcessor(TestScenarioState testScenarioState, TestStepResultValidator validator,
                                     Map<String, IbmMqChannel> ibmMqChannels) {
        super(testScenarioState, validator);
        this.ibmMqChannels = ibmMqChannels;
    }

    @Override
    protected Object processTestStep(IbmMqTestStep testStep) {
        Object response = null;
        Optional<String> sendTo = testStep.getSendTo();
        Optional<String> receiveFrom = testStep.getReceiveFrom();
        Optional<String> respondTo = testStep.getReceiveFrom();

        sendTo.ifPresent(channel -> send(testStep.getTestData(), channel));

        if (receiveFrom.isPresent()) {
            IbmMqChannel ibmMqChannel = ibmMqChannels.get(receiveFrom.get());
            response = ibmMqChannel.read(testStep.getDelay(), testStep.getTimeout());

            respondTo.ifPresent(channel -> send(testStep.getTestData(), channel));
        }
        return response;
    }

    private void send(Object testData, String channel) {
        IbmMqChannel ibmMqChannel = ibmMqChannels.get(channel);
        ibmMqChannel.send(testData);
    }

    @Override
    public Class<IbmMqTestStep> getTestStepClass() {
        return IbmMqTestStep.class;
    }
}
