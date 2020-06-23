package org.skellig.runner.config;

import org.skellig.teststep.processing.converter.TestDataConverter;
import org.skellig.teststep.runner.context.SkelligTestContext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestSkelligContext extends SkelligTestContext {

    @Override
    protected List<TestDataConverter> getAdditionalTestDataConverters() {
        return Collections.singletonList(new CustomMessageTestDataConverter());
    }

    @Override
    protected List<TestStepProcessorDetails> getTestStepProcessors() {
        return Stream.of(
                new TestStepProcessorDetails(
                        new SimpleMessageTestStepProcessor.Builder()
                        .withTestScenarioState(getTestScenarioState())
                        .withValidator(getTestStepResultValidator())
                        .build(),
                        createTestStepFactoryFrom(SimpleMessageTestStepFactory::new)
                )
        ).collect(Collectors.toList());
    }
}