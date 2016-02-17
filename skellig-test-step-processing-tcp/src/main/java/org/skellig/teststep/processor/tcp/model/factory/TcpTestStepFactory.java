package org.skellig.teststep.processor.tcp.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;
import org.skellig.teststep.processor.tcp.model.TcpTestStep;

import java.util.Map;
import java.util.Properties;

public class TcpTestStepFactory extends BaseTestStepFactory {

    private static final String SEND_TO_KEYWORD = "test.step.keyword.sendTo";
    private static final String READ_FROM_KEYWORD = "test.step.keyword.readFrom";

    public TcpTestStepFactory(Properties keywordsProperties,
                              TestStepValueConverter testStepValueConverter,
                              TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return new TcpTestStep.Builder()
                .withReadFrom((String) rawTestStep.get(getReadFromKeyword()))
                .withSendTo((String) rawTestStep.get(getSendToKeyword()));
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey(getSendToKeyword()) || rawTestStep.containsKey(getReadFromKeyword());
    }

    private String getSendToKeyword() {
        return getKeywordName(SEND_TO_KEYWORD, "sendTo");
    }

    private String getReadFromKeyword() {
        return getKeywordName(READ_FROM_KEYWORD, "readFrom");
    }
}
