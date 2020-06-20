package org.skellig.teststep.processing.http.model.factory;

import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.http.model.HttpTestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpTestStepFactory extends BaseTestStepFactory {

    private static final Pattern PATTERN = Pattern.compile(",");

    private static final String SERVICE_KEYWORD = "test.step.keyword.service";
    private static final String URL_KEYWORD = "test.step.keyword.url";
    private static final String METHOD_KEYWORD = "test.step.keyword.http_method";
    private static final String HEADERS_KEYWORD = "test.step.keyword.http_headers";
    private static final String QUERY_KEYWORD = "test.step.keyword.http_query";
    private static final String FORM_KEYWORD = "test.step.keyword.form";
    private static final String USER_KEYWORD = "test.step.keyword.username";
    private static final String PASSWORD_KEYWORD = "test.step.keyword.password";

    public HttpTestStepFactory(Properties keywordsProperties, TestStepValueConverter testStepValueConverter) {
        super(keywordsProperties, testStepValueConverter);
    }

    @Override
    protected CreateTestStepDelegate createTestStep(Map<String, Object> rawTestStep) {
        return (id, name, testData, validationDetails) ->
                new HttpTestStep.Builder()
                        .withService(getServices(rawTestStep))
                        .withUrl(convertValue(rawTestStep.get(getUrlKeyword())))
                        .withMethod((String) rawTestStep.get(getMethodKeyword()))
                        .withHeaders(getHttpHeaders(rawTestStep))
                        .withQuery(getHttpQuery(rawTestStep))
                        .withForm(getForm(rawTestStep))
                        .withUsername(convertValue(rawTestStep.get(getKeywordName(USER_KEYWORD, "username"))))
                        .withPassword(convertValue(rawTestStep.get(getKeywordName(PASSWORD_KEYWORD, "password"))))
                        .withId(id)
                        .withName(name)
                        .withTestData(testData)
                        .withValidationDetails(validationDetails)
                        .build();
    }

    private Map<String, String> getForm(Map<String, Object> rawTestStep) {
        return ((Map<String, String>) rawTestStep.get(getKeywordName(FORM_KEYWORD, "form"))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertValue(entry.getValue())));
    }

    private Map<String, String> getHttpQuery(Map<String, Object> rawTestStep) {
        return ((Map<String, String>) rawTestStep.get(getKeywordName(QUERY_KEYWORD, "http_query"))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertValue(entry.getValue())));
    }

    private Map<String, String> getHttpHeaders(Map<String, Object> rawTestStep) {
        return ((Map<String, String>) rawTestStep.get(getKeywordName(HEADERS_KEYWORD, "http_headers"))).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertValue(entry.getValue())));
    }

    private Collection<String> getServices(Map<String, Object> rawTestStep) {
        Object rawServices = rawTestStep.get(getKeywordName(SERVICE_KEYWORD, "service"));
        if (rawServices != null) {
            if (rawServices instanceof String) {
                return Stream.of(PATTERN.split(convertValue(rawServices))).collect(Collectors.toList());
            } else if (rawServices instanceof Collection) {
                return (Collection<String>) rawServices;
            }
        }
        return null;
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
