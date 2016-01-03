package org.skellig.teststep.reader.model.factory;

import java.util.Map;

public abstract class BaseTestStepFactory implements TestStepFactory {

    protected String getId(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey("id") ? String.valueOf(rawTestStep.get("id")) : null;
    }

    protected String getName(Map<String, Object> rawTestStep) {
        return String.valueOf(rawTestStep.get("name"));
    }

    protected Object getTestData(Map<String, Object> rawTestStep) {
        return rawTestStep.get("data");
    }
}
