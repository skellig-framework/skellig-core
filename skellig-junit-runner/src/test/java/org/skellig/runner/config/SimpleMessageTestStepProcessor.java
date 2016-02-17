package org.skellig.runner.config;

import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.skellig.task.TaskUtils.runTask;

public class SimpleMessageTestStepProcessor extends BaseTestStepProcessor<SimpleMessageTestStep> {

    private Map<Object, Object> latestReceivedMessage;

    protected SimpleMessageTestStepProcessor(TestScenarioState testScenarioState,
                                             TestStepResultValidator validator) {
        super(testScenarioState, validator);
    }

    @Override
    protected Object processTestStep(SimpleMessageTestStep testStep) {
        if (testStep.getReceiveFrom() != null) {
             Map<Object, Object> response =
                    runTask(() -> latestReceivedMessage, Objects::nonNull, 500, 3000);
            response.put("receivedFrom", testStep.getReceiveFrom());
            return response;
        } else {
            latestReceivedMessage = createResponse(testStep);
            return latestReceivedMessage;
        }
    }

    private Map<Object, Object> createResponse(SimpleMessageTestStep testStep) {
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
