package org.skellig.teststep.processing.db.model.factory;

import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.db.model.DatabaseTestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;

import java.util.Map;
import java.util.Properties;

public class DatabaseTestStepFactory extends BaseTestStepFactory {

    private static final String TABLE_KEYWORD = "test.step.keyword.table";
    private static final String COMMAND_KEYWORD = "test.step.keyword.command";
    private static final String QUERY_KEYWORD = "test.step.keyword.query";

    public DatabaseTestStepFactory(TestStepValueConverter testStepValueConverter) {
        this(null, testStepValueConverter);
    }

    public DatabaseTestStepFactory(Properties keywordsProperties,
                                   TestStepValueConverter testStepValueConverter) {
        super(keywordsProperties, testStepValueConverter);
    }

    @Override
    protected CreateTestStepDelegate createTestStep(Map<String, Object> rawTestStep) {
        return (id, name, testData, validationDetails) ->
                new DatabaseTestStep.Builder()
                        .withCommand(convertValue(rawTestStep.get(getKeywordName(COMMAND_KEYWORD, "command"))))
                        .withTable(convertValue(rawTestStep.get(getTableKeyword())))
                        .withQuery(convertValue(rawTestStep.get(getQueryKeyword())))
                        .withId(id)
                        .withName(name)
                        .withTestData(testData)
                        .withValidationDetails(validationDetails)
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
