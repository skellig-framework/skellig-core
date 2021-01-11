package org.skellig.teststep.processor.http.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;
import org.skellig.teststep.processor.http.model.HttpTestStep;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class HttpTestStepFactory extends BaseTestStepFactory {

    private static final String SERVICE_KEYWORD = "test.step.keyword.service";
    private static final String URL_KEYWORD = "test.step.keyword.url";
    private static final String METHOD_KEYWORD = "test.step.keyword.http_method";
    private static final String HEADERS_KEYWORD = "test.step.keyword.http_headers";
    private static final String QUERY_KEYWORD = "test.step.keyword.http_query";
    private static final String FORM_KEYWORD = "test.step.keyword.form";
    private static final String USER_KEYWORD = "test.step.keyword.username";
    private static final String PASSWORD_KEYWORD = "test.step.keyword.password";

    public HttpTestStepFactory(Properties keywordsProperties,
                               TestStepValueConverter testStepValueConverter,
                               TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        Collection<String> services =
                getStringArrayDataFromRawTestStep(getKeywordName(SERVICE_KEYWORD, "service"), rawTestStep, parameters);

        return new HttpTestStep.Builder()
                .withService(services)
                .withUrl(convertValue(rawTestStep.get(getUrlKeyword()), parameters))
                .withMethod((String) rawTestStep.get(getMethodKeyword()))
                .withHeaders(getHttpHeaders(rawTestStep, parameters))
                .withQuery(getHttpQuery(rawTestStep, parameters))
                .withForm(getForm(rawTestStep, parameters))
                .withUsername(convertValue(rawTestStep.get(getKeywordName(USER_KEYWORD, "username")), parameters))
                .withPassword(convertValue(rawTestStep.get(getKeywordName(PASSWORD_KEYWORD, "password")), parameters));
    }

    private Map<String, String> getForm(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return ((Map<String, String>) rawTestStep.get(getKeywordName(FORM_KEYWORD, "form"))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertValue(entry.getValue(), parameters)));
    }

    private Map<String, String> getHttpQuery(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return ((Map<String, String>) rawTestStep.get(getKeywordName(QUERY_KEYWORD, "http_query"))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertValue(entry.getValue(), parameters)));
    }

    private Map<String, String> getHttpHeaders(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return ((Map<String, String>) rawTestStep.get(getKeywordName(HEADERS_KEYWORD, "http_headers"))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertValue(entry.getValue(), parameters)));
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