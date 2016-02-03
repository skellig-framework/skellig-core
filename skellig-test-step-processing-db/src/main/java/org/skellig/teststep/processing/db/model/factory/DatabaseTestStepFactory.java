package org.skellig.teststep.processing.db.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.db.model.DatabaseTestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;

import java.util.Map;
import java.util.Properties;

public class DatabaseTestStepFactory extends BaseTestStepFactory {

    private static final String TABLE_KEYWORD = "test.step.keyword.table";
    private static final String COMMAND_KEYWORD = "test.step.keyword.command";
    private static final String QUERY_KEYWORD = "test.step.keyword.query";

    public DatabaseTestStepFactory(Properties keywordsProperties,
                                   TestStepValueConverter testStepValueConverter,
                                   TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected CreateTestStepDelegate createTestStep(Map<String, Object> rawTestStep) {
        return (id, name, testData, validationDetails, parameters, variables) ->
                new DatabaseTestStep.Builder()
                        .withCommand(convertValue(rawTestStep.get(getKeywordName(COMMAND_KEYWORD, "command")), parameters))
                        .withTable(convertValue(rawTestStep.get(getTableKeyword()), parameters))
                        .withQuery(convertValue(rawTestStep.get(getQueryKeyword()), parameters))
                        .withId(id)
                        .withName(name)
                        .withTestData(testData)
                        .withValidationDetails(validationDetails)
                        .withVariables(variables)
                        .build();
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey(getTableKeyword()) || rawTestStep.containsKey(getQueryKeyword());
    }

    private String getQueryKeyword() {
        return getKeywordName(QUERY_KEYWORD, "query");
    }

    private String getTableKeyword() {
        return getKeywordName(TABLE_KEYWORD, "table");
    }
}
