package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.HttpTestStep;
import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;
import java.util.Properties;

public class HttpTestStepFactory extends BaseTestStepFactory {

    private static final String URL_KEYWORD = "test.step.url";
    private static final String METHOD_KEYWORD = "test.step.http_method";
    private static final String HEADERS_KEYWORD = "test.step.http_headers";
    private static final String QUERY_KEYWORD = "test.step.http_query";
    private static final String FORM_KEYWORD = "test.step.form";
    private static final String USER_KEYWORD = "test.step.username";
    private static final String PASSWORD_KEYWORD = "test.step.password";

    public HttpTestStepFactory() {
    }

    public HttpTestStepFactory(Properties keywordsProperties) {
        super(keywordsProperties);
    }

    @Override
    public TestStep create(Map<String, Object> rawTestStep) {
        return new HttpTestStep.Builder()
                .withUrl((String) rawTestStep.get(getUrlKeyword()))
                .withMethod((String) rawTestStep.get(getMethodKeyword()))
                .withHeaders((Map<String, String>) rawTestStep.get(getKeywordName(HEADERS_KEYWORD, "http_headers")))
                .withQuery((Map<String, String>) rawTestStep.get(getKeywordName(QUERY_KEYWORD, "http_query")))
                .withForm((Map<String, String>) rawTestStep.get(getKeywordName(FORM_KEYWORD, "form")))
                .withUsername((String) rawTestStep.get(getKeywordName(USER_KEYWORD, "username")))
                .withPassword((String) rawTestStep.get(getKeywordName(PASSWORD_KEYWORD, "password")))
                .withId(getId(rawTestStep))
                .withName(getName(rawTestStep))
                .withTestData(getTestData(rawTestStep))
                .withValidationDetails(createValidationDetails(rawTestStep))
                .build();
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey(URL_KEYWORD);
    }

    private String getMethodKeyword() {
        return getKeywordName(METHOD_KEYWORD, "http_method");
    }

    private String getUrlKeyword() {
        return getKeywordName(URL_KEYWORD, "url");
    }
}
