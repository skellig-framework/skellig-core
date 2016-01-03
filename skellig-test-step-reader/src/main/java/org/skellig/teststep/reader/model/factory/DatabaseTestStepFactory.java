package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.DatabaseTestStep;
import org.skellig.teststep.reader.model.HttpTestStep;
import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;

public class DatabaseTestStepFactory extends BaseTestStepFactory {

    private static final String URL_KEYWORD = "url";

    @Override
    public TestStep create(Map<String, Object> rawTestStep) {
        return new DatabaseTestStep.Builder()
                .withCommand((String) rawTestStep.get("command"))
                .withTable((String) rawTestStep.get("table"))
                .withQuery((String) rawTestStep.get("query"))
                .withId(getId(rawTestStep))
                .withName(getName(rawTestStep))
                .withTestData(getTestData(rawTestStep))
                .build();
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey(URL_KEYWORD);
    }
}
