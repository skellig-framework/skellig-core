package org.skellig.teststep.processing.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.utils.CachedPattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseTestStepFactory implements TestStepFactory {


    private static final String TEST_STEP_NAME_KEYWORD = "test.step.keyword.name";
    private static final String VARIABLES_KEYWORD = "test.step.keyword.variables";

    private static Set<String> testDataKeywords;

    private Properties keywordsProperties;
    private TestDataConverter testDataConverter;
    private TestStepFactoryValueConverter testStepFactoryValueConverter;
    private ValidationDetailsFactory validationDetailsFactory;

    public BaseTestStepFactory(Properties keywordsProperties,
                               TestStepValueConverter testStepValueConverter,
                               TestDataConverter testDataConverter) {

        this.keywordsProperties = keywordsProperties;
        this.testStepFactoryValueConverter = new TestStepFactoryValueConverter(testStepValueConverter);
        this.validationDetailsFactory = new ValidationDetailsFactory(keywordsProperties, testStepFactoryValueConverter);
        this.testDataConverter = testDataConverter;

        if (testDataKeywords == null) {
            testDataKeywords = Stream.of(
                    getKeywordName("test.step.keyword.data", "data"),
                    getKeywordName("test.step.keyword.payload", "payload"),
                    getKeywordName("test.step.keyword.body", "body"),
                    getKeywordName("test.step.keyword.request", "request"),
                    getKeywordName("test.step.keyword.response", "response"),
                    getKeywordName("test.step.keyword.message", "message"))
                    .collect(Collectors.toSet());
        }

    }

    @Override
    public TestStep create(String testStepName, Map<String, Object> rawTestStep, Map<String, String> parameters) {
        Map<String, Object> additionalParameters = new HashMap<>(parameters);

        Map<String, String> parametersFromTestName = extractParametersFromTestStepName(testStepName, rawTestStep);
        if (parametersFromTestName != null) {
            additionalParameters.putAll(parametersFromTestName);
        }

        Map<String, Object> variables = extractVariables(rawTestStep, additionalParameters);
        if (variables != null) {
            additionalParameters.putAll(variables);
        }

        return createTestStep(rawTestStep).create(
                getId(rawTestStep),
                testStepName,
                extractTestData(rawTestStep, additionalParameters),
                validationDetailsFactory.create(rawTestStep, additionalParameters),
                additionalParameters,
                variables);
    }

    protected abstract CreateTestStepDelegate createTestStep(Map<String, Object> rawTestStep);

    private Map<String, Object> extractVariables(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        Object rawVariables = rawTestStep.get(getKeywordName(VARIABLES_KEYWORD, "variables"));
        Object convertedVariables = convertHierarchicalData(rawVariables, parameters);
        return convertedVariables instanceof Map ? (Map<String, Object>) convertedVariables : null;
    }

    private Object extractTestData(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        return testDataKeywords.stream()
                .filter(rawTestStep::containsKey)
                .map(keyword -> testDataConverter.convert(convertHierarchicalData(rawTestStep.get(keyword), parameters)))
                .findFirst()
                .orElse(null);
    }

    private Object convertHierarchicalData(Object data, Map<String, Object> parameters) {
        if (data instanceof Map) {
            return ((Map<String, Object>) data).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> convertHierarchicalData(entry.getValue(), parameters)));
        } else if (data instanceof List) {
            return ((List) data).stream()
                    .map(item -> convertHierarchicalData(item, parameters))
                    .collect(Collectors.toList());
        } else {
            return testStepFactoryValueConverter.convertValue(data, parameters);
        }
    }

    private Map<String, String> extractParametersFromTestStepName(String testStepName, Map<String, Object> rawTestStep) {
        Map<String, String> parameters = null;
        Matcher matcher = CachedPattern.compile(getName(rawTestStep)).matcher(testStepName);
        if (matcher.find()) {
            parameters = new HashMap<>();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                parameters.put("$" + i, matcher.group(i));
            }
        }
        return parameters;
    }

    protected String getId(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey("id") ? String.valueOf(rawTestStep.get("id")) : null;
    }

    protected String getName(Map<String, Object> rawTestStep) {
        return String.valueOf(rawTestStep.get(getKeywordName(TEST_STEP_NAME_KEYWORD, "name")));
    }

    protected String getKeywordName(String keywordName, String defaultValue) {
        return keywordsProperties == null ? defaultValue : keywordsProperties.getProperty(keywordName, defaultValue);
    }

    protected <T> T convertValue(Object value, Map<String, Object> parameters) {
        return testStepFactoryValueConverter.convertValue(value, parameters);
    }

    protected interface CreateTestStepDelegate {

        TestStep create(String id, String name, Object testData, ValidationDetails validationDetails,
                        Map<String, Object> parameters, Map<String, Object> variables);
    }

}
