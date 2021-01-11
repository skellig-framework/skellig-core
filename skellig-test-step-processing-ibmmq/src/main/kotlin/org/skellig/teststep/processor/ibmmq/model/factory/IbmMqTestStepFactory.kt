package org.skellig.teststep.processor.ibmmq.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;
import org.skellig.teststep.processor.ibmmq.model.IbmMqTestStep;

import java.util.Map;
import java.util.Properties;

public class IbmMqTestStepFactory extends BaseTestStepFactory {

    private static final String PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol";
    private static final String SEND_TO_KEYWORD = "test.step.keyword.sendTo";
    private static final String RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom";
    private static final String RESPOND_TO_KEYWORD = "test.step.keyword.respondTo";
    private static final String IBMMQ = "ibmmq";

    public IbmMqTestStepFactory(Properties keywordsProperties,
                                TestStepValueConverter testStepValueConverter,
                                TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return new IbmMqTestStep.Builder()
                .withSendTo(convertValue(rawTestStep.get(getSendToKeyword()), parameters))
                .withReceiveFrom(convertValue(rawTestStep.get(getReceiveFromKeyword()), parameters))
                .withRespondTo(convertValue(rawTestStep.get(getRespondToKeyword()), parameters));
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "").equals(IBMMQ);
    }

    private String getSendToKeyword() {
        return getKeywordName(SEND_TO_KEYWORD, "sendTo");
    }

    private String getReceiveFromKeyword() {
        return getKeywordName(RECEIVE_FROM_KEYWORD, "receiveFrom");
    }

    private String getRespondToKeyword() {
        return getKeywordName(RESPOND_TO_KEYWORD, "respondTo");
    }
}
