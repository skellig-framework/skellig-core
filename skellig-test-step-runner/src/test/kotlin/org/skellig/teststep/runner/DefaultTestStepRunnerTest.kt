package org.skellig.teststep.runner;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.factory.TestStepFactory;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.reader.TestStepReader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Run test step")
class DefaultTestStepRunnerTest {

    private TestStepRunner testStepRunner;
    private TestStepProcessor testStepProcessor;
    private TestStepReader testStepReader;
    private TestStep testStep;
    private TestStepFactory testStepFactory;

    @BeforeEach
    void setUp() {
        testStepProcessor = mock(TestStepProcessor.class);
        testStepReader = mock(TestStepReader.class);
        testStepFactory = mock(TestStepFactory.class);
    }

    @Test
    @DisplayName("When no parameters extracted from name")
    void testRunTestStepWithoutParameters() {
        String testStepName = "test1";
        initializeTestSteps(testStepName, Collections.emptyMap());
        initializeTestStepRunner();

        testStepRunner.run(testStepName);

        verify(testStepProcessor).process(testStep);
    }

    @Test
    @DisplayName("When step doesn't exist Then throw exception")
    void testRunTestStepWhenNotExist() {
        initializeTestSteps("test1", Collections.emptyMap());
        initializeTestStepRunner();

        assertThrows(TestStepProcessingException.class, () -> testStepRunner.run("test2"));

        verifyZeroInteractions(testStepProcessor);
    }

    private void initializeTestSteps(String testStepName, Map<String, String> parameters) {
        Map<String, Object> rawTestStep = Collections.singletonMap("name", testStepName);

        testStep = new TestStep.Builder()
                .withId("t1")
                .withName(testStepName)
                .build();

        when(testStepFactory.create(testStepName, rawTestStep, parameters)).thenReturn(testStep);

        when(testStepReader.read(any(Path.class)))
                .thenReturn(Stream.of(rawTestStep)
                        .collect(Collectors.toList()));
    }

    private void initializeTestStepRunner() {
        testStepRunner = new DefaultTestStepRunner.Builder()
                .withTestStepFactory(testStepFactory)
                .withTestStepProcessor(testStepProcessor)
                .withTestStepReader(testStepReader, getClass().getClassLoader(), Collections.singletonList("steps"))
                .build();
    }
}