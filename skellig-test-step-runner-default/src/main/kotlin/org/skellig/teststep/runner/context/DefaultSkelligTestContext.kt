package org.skellig.teststep.runner.context;

import com.typesafe.config.Config;
import org.skellig.teststep.processor.http.HttpTestStepProcessor;
import org.skellig.teststep.processor.http.model.factory.HttpTestStepFactory;

import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultSkelligTestContext extends SkelligTestContext {

    private static final String TEST_STEP_KEYWORD = "test.step.keyword";

    private Config config;
    private Properties keywordProperties;

    public DefaultSkelligTestContext(Config config) {
        this.config = config;

        if (config.hasPath(TEST_STEP_KEYWORD)) {
            keywordProperties = new Properties();
            config.getObject(TEST_STEP_KEYWORD)
                    .forEach((key, value) ->
                            keywordProperties.setProperty(TEST_STEP_KEYWORD + "." + key, String.valueOf(value)));
        }
    }

    @Override
    protected List<TestStepProcessorDetails> getTestStepProcessors() {
        return Stream.of(
                new TestStepProcessorDetails(
                        new HttpTestStepProcessor.Builder()
                                .withHttpService(config)
                                .withTestScenarioState(getTestScenarioState())
                                .withValidator(getTestStepResultValidator())
                                .build(),
                        createTestStepFactoryFrom(HttpTestStepFactory::new)
                )
        ).collect(Collectors.toList());
    }

    @Override
    protected Properties getTestStepKeywordsProperties() {
        return keywordProperties;
    }

    @Override
    protected Function<String, String> getPropertyExtractorFunction() {
        return key -> config.getString(key);
    }
}
