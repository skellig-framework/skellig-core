package org.skellig.teststep.processor.rmq.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;
import org.skellig.teststep.processor.rmq.model.RmqTestStep;

import java.util.Map;
import java.util.Properties;

public class RmqTestStepFactory extends BaseTestStepFactory {

    private static final String PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol";
    private static final String ROUTING_KEY_KEYWORD = "test.step.keyword.routingKey";
    private static final String SEND_TO_KEYWORD = "test.step.keyword.sendTo";
    private static final String RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom";
    private static final String RESPOND_TO_KEYWORD = "test.step.keyword.respondTo";
    private static final String RMQ = "rmq";

    public RmqTestStepFactory(Properties keywordsProperties, TestStepValueConverter testStepValueConverter, TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return new RmqTestStep.Builder()
                .withSendTo(convertValue(rawTestStep.get(getSendToKeyword()), parameters))
                .withReceiveFrom(convertValue(rawTestStep.get(getReceiveFromKeyword()), parameters))
                .withRespondTo(convertValue(rawTestStep.get(getRespondToKeyword()), parameters))
                .withRoutingKey(convertValue(getRoutingKey(rawTestStep), parameters));
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return getRoutingKey(rawTestStep) != null ||
                rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "").equals(RMQ);
    }

    private Object getRoutingKey(Map<String, Object> rawTestStep) {
        return rawTestStep.get(getKeywordName(ROUTING_KEY_KEYWORD, "routingKey"));
    }

    private String getSendToKeyword() {
        return getKeywordName(SEND_TO_KEYWORD, "sendTo");
    }

    private String getReceiveFromKeyword() {
        return getKeywordName(RECEIVE_FROM_KEYWORD, "readFrom");
    }

    private String getRespondToKeyword() {
        return getKeywordName(RESPOND_TO_KEYWORD, "respondTo");
    }
}
