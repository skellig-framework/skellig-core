package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.HttpTestStep;
import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;

public class HttpTestStepFactory extends BaseTestStepFactory {

    private static final String URL_KEYWORD = "url";

    @Override
    public TestStep create(Map<String, Object> rawTestStep) {
        return new HttpTestStep.Builder()
                .withUrl((String) rawTestStep.get(URL_KEYWORD))
                .withMethod((String) rawTestStep.get("method"))
                .withHeaders((Map<String, String>) rawTestStep.get("headers"))
                .withQuery((Map<String, String>) rawTestStep.get("query"))
                .withForm((Map<String, String>) rawTestStep.get("form"))
                .withUsername((String) rawTestStep.get("username"))
                .withPassword((String) rawTestStep.get("password"))
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
