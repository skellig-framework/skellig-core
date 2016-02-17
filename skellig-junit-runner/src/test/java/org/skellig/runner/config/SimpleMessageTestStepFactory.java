package org.skellig.runner.config;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;

import java.util.Map;
import java.util.Properties;

public class SimpleMessageTestStepFactory extends BaseTestStepFactory {

    public SimpleMessageTestStepFactory(Properties keywordsProperties, TestStepValueConverter testStepValueConverter,
                                        TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return new SimpleMessageTestStep.Builder()
                .withReceiver(convertValue(rawTestStep.get("receiver"), parameters))
                .withReceiveFrom(convertValue(rawTestStep.get("receiveFrom"), parameters));
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey("receiver") || rawTestStep.containsKey("receiveFrom");
    }
}
