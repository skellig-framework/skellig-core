package org.skellig.teststep.reader.model.factory;

import org.skellig.teststep.reader.model.DatabaseTestStep;
import org.skellig.teststep.reader.model.TestStep;

import java.util.Map;
import java.util.Properties;

public class DatabaseTestStepFactory extends BaseTestStepFactory {

    private static final String TABLE_KEYWORD = "test.step.table";
    private static final String COMMAND_KEYWORD = "test.step.command";
    private static final String QUERY_KEYWORD = "test.step.query";

    public DatabaseTestStepFactory() {
    }

    public DatabaseTestStepFactory(Properties keywordsProperties) {
        super(keywordsProperties);
    }

    @Override
    public TestStep create(Map<String, Object> rawTestStep) {
        return new DatabaseTestStep.Builder()
                .withCommand((String) rawTestStep.get(getKeywordName(COMMAND_KEYWORD, "command")))
                .withTable((String) rawTestStep.get(getTableKeyword()))
                .withQuery((String) rawTestStep.get(getQueryKeyword()))
                .withId(getId(rawTestStep))
                .withName(getName(rawTestStep))
                .withTestData(getTestData(rawTestStep))
                .withValidationDetails(createValidationDetails(rawTestStep))
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
