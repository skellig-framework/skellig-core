package org.skellig.teststep.processing.db.model.factory;

import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.db.model.DatabaseTestStep;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;

import java.util.Map;
import java.util.Properties;

public class DatabaseTestStepFactory extends BaseTestStepFactory {

    private static final String TABLE_KEYWORD = "test.step.table";
    private static final String COMMAND_KEYWORD = "test.step.command";
    private static final String QUERY_KEYWORD = "test.step.query";

    public DatabaseTestStepFactory(TestStepValueConverter testStepValueConverter) {
        this(null, testStepValueConverter);
    }

    public DatabaseTestStepFactory(Properties keywordsProperties,
                                   TestStepValueConverter testStepValueConverter) {
        super(keywordsProperties, testStepValueConverter);
    }

    @Override
    public TestStep create(Map<String, Object> rawTestStep) {
        return new DatabaseTestStep.Builder()
                .withCommand(convertValue(rawTestStep.get(getKeywordName(COMMAND_KEYWORD, "command"))))
                .withTable(convertValue(rawTestStep.get(getTableKeyword())))
                .withQuery(convertValue(rawTestStep.get(getQueryKeyword())))
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
