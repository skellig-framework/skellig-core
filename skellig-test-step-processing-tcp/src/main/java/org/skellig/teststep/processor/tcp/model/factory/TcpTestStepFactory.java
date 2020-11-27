package org.skellig.teststep.processor.tcp.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;
import org.skellig.teststep.processor.tcp.model.TcpTestStep;

import java.util.Map;
import java.util.Properties;

public class TcpTestStepFactory extends BaseTestStepFactory {

    private static final String PROTOCOL_KEY_KEYWORD = "test.step.keyword.protocol";
    private static final String SEND_TO_KEYWORD = "test.step.keyword.sendTo";
    private static final String RECEIVE_FROM_KEYWORD = "test.step.keyword.receiveFrom";
    private static final String RESPOND_TO_KEYWORD = "test.step.keyword.respondTo";
    private static final String BUFFER_SIZE_KEYWORD = "test.step.keyword.bufferSize";
    private static final String TCP = "tcp";

    public TcpTestStepFactory(Properties keywordsProperties,
                              TestStepValueConverter testStepValueConverter,
                              TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        TcpTestStep.Builder builder = new TcpTestStep.Builder()
                .withSendTo(convertValue(rawTestStep.get(getSendToKeyword()), parameters))
                .withReceiveFrom(convertValue(rawTestStep.get(getReceiveFromKeyword()), parameters))
                .withRespondTo(convertValue(rawTestStep.get(getRespondToKeyword()), parameters));

        Object readBufferSize = rawTestStep.get(getReadBufferSizeKeyword());
        if(readBufferSize != null) {
            builder.withReadBufferSize(convertValue(readBufferSize, parameters));
        }
        return builder;
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.getOrDefault(getKeywordName(PROTOCOL_KEY_KEYWORD, "protocol"), "").equals(TCP);
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

    private String getReadBufferSizeKeyword() {
        return getKeywordName(BUFFER_SIZE_KEYWORD, "bufferSize");
    }
}
