package org.skellig.runner.config;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;

import java.util.Map;
import java.util.Properties;

public class SimpleMessageTestStepFactory extends BaseTestStepFactory {

    public SimpleMessageTestStepFactory(Properties keywordsProperties, TestStepValueConverter testStepValueConverter,
                                        TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected CreateTestStepDelegate createTestStep(Map<String, Object> rawTestStep) {
        return (id, name, testData, validationDetails, parameters, variables) ->
                new SimpleMessageTestStep.Builder()
                        .withReceiver(convertValue(rawTestStep.get("receiver"), parameters))
                        .withId(id)
                        .withName(name)
                        .withVariables(variables)
                        .withTestData(testData)
                        .withValidationDetails(validationDetails)
                        .build();
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey("receiver");
    }
}
