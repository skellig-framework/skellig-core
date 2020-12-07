package org.skellig.teststep.processor.db.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.exception.TestDataConversionException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;
import org.skellig.teststep.processor.db.model.DatabaseTestStep;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseTestStepFactory extends BaseTestStepFactory {

    private static final String SERVERS_KEYWORD = "test.step.keyword.servers";
    private static final String TABLE_KEYWORD = "test.step.keyword.table";
    private static final String COMMAND_KEYWORD = "test.step.keyword.command";
    private static final String QUERY_KEYWORD = "test.step.keyword.query";
    private static final String WHERE_KEYWORD = "test.step.keyword.where";
    private static final String VALUES_KEYWORD = "test.step.keyword.values";

    private final Set<String> dbTestDataKeywords;

    public DatabaseTestStepFactory(Properties keywordsProperties,
                                   TestStepValueConverter testStepValueConverter,
                                   TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
        dbTestDataKeywords = Stream.of(
                getKeywordName(WHERE_KEYWORD, "where"),
                getKeywordName(VALUES_KEYWORD, "values"))
                .collect(Collectors.toSet());
    }

    @Override
    public TestStep create(String testStepName, Map<String, Object> rawTestStep, Map<String, String> parameters) {
        TestStep testStep = super.create(testStepName, rawTestStep, parameters);
        if (testStep.getTestData() != null && !(testStep.getTestData() instanceof Map)) {
            throw new TestDataConversionException("Test Data of Database Test Step must be class of Map<String,Object>");
        }

        return testStep;
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        Collection<String> servers =
                getStringArrayDataFromRawTestStep(getKeywordName(SERVERS_KEYWORD, "servers"), rawTestStep, parameters);
        return new DatabaseTestStep.Builder()
                .withServers(servers)
                .withCommand(convertValue(rawTestStep.get(getKeywordName(COMMAND_KEYWORD, "command")), parameters))
                .withTable(convertValue(rawTestStep.get(getTableKeyword()), parameters))
                .withQuery(convertValue(rawTestStep.get(getQueryKeyword()), parameters));
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey(getTableKeyword()) || rawTestStep.containsKey(getQueryKeyword());
    }

    @Override
    protected Set<String> getTestDataKeywords() {
        return dbTestDataKeywords;
    }

    private String getQueryKeyword() {
        return getKeywordName(QUERY_KEYWORD, "query");
    }

    private String getTableKeyword() {
        return getKeywordName(TABLE_KEYWORD, "table");
    }
}