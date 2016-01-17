package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class ValidationTestStepFactory extends BaseTestStepFactory {

    public ValidationTestStepFactory() {
    }

    public ValidationTestStepFactory(Properties keywordsProperties) {
        super(keywordsProperties);
    }

    @Override
    public TestStep create(Map<String, Object> rawTestStep) {
        return new TestStep.Builder<TestStep>()
                .withId(getId(rawTestStep))
                .withName(getName(rawTestStep))
                .withValidationDetails(createValidationDetails(rawTestStep))
                .build();
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        Optional<Object> validationDetails = getValidationDetails(rawTestStep);
        return validationDetails.isPresent() &&
                validationDetails.get() instanceof Map &&
                ((Map) validationDetails.get()).containsKey(getFromTestKeyword());
    }
}
