package org.skellig.teststep.processor.unix.model.factory;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.processing.converter.TestStepValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.BaseTestStepFactory;
import org.skellig.teststep.processor.unix.model.UnixShellTestStep;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class UnixShellTestStepFactory extends BaseTestStepFactory {

    private static final String HOSTS_KEYWORD = "test.step.keyword.hosts";
    private static final String COMMAND_KEYWORD = "test.step.keyword.command";
    private static final String ARGS_KEYWORD = "test.step.keyword.args";

    public UnixShellTestStepFactory(Properties keywordsProperties,
                                    TestStepValueConverter testStepValueConverter,
                                    TestDataConverter testDataConverter) {
        super(keywordsProperties, testStepValueConverter, testDataConverter);
    }

    @Override
    protected TestStep.Builder createTestStepBuilder(Map<String, Object> rawTestStep, Map<String, Object> parameters) {
        Collection<String> services =
                getStringArrayDataFromRawTestStep(getKeywordName(HOSTS_KEYWORD, "hosts"), rawTestStep, parameters);

        return new UnixShellTestStep.Builder()
                .withHosts(services)
                .withCommand(convertValue(rawTestStep.get(getCommandKeyword()), parameters))
                .withArgs(convertValue(rawTestStep.get(getKeywordName(ARGS_KEYWORD, "args")), parameters));
    }

    @Override
    public boolean isConstructableFrom(Map<String, Object> rawTestStep) {
        return rawTestStep.containsKey(getCommandKeyword());
    }

    private String getCommandKeyword() {
        return getKeywordName(COMMAND_KEYWORD, "command");
    }

}
