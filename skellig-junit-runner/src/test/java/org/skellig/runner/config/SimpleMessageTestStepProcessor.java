package org.skellig.runner.config;

import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

import java.util.HashMap;
import java.util.Map;

public class SimpleMessageTestStepProcessor extends BaseTestStepProcessor<SimpleMessageTestStep> {

    protected SimpleMessageTestStepProcessor(TestScenarioState testScenarioState,
                                             TestStepResultValidator validator) {
        super(testScenarioState, validator);
    }

    @Override
    protected Object processTestStep(SimpleMessageTestStep testStep) {
        Map<Object, Object> response = new HashMap<>();
        response.put("originalRequest", testStep.getTestData());
        response.put("receivedBy", testStep.getReceiver());
        response.put("status", "success");

        return response;
    }

    @Override
    public Class<SimpleMessageTestStep> getTestStepClass() {
        return SimpleMessageTestStep.class;
    }


    public static class Builder {
        private TestScenarioState testScenarioState;
        private TestStepResultValidator validator;

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withValidator(TestStepResultValidator validator) {
            this.validator = validator;
            return this;
        }

        public TestStepProcessor<SimpleMessageTestStep> build() {
            return new SimpleMessageTestStepProcessor(testScenarioState, validator);
        }
    }
}
