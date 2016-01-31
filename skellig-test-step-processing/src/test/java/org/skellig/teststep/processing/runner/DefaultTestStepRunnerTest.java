package org.skellig.teststep.processing.runner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.reader.TestStepReader;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

    @BeforeEach
    void setUp() {
        testStepProcessor = mock(TestStepProcessor.class);
        testStepReader = mock(TestStepReader.class);
    }

    @Test
    @DisplayName("When no parameters extracted from name")
    void testRunTestStepWithoutParameters() throws URISyntaxException {
        String testStepName = "test1";
        initializeTestSteps(testStepName);
        initializeTestStepRunner();

        testStepRunner.run(testStepName);

        verify(testStepProcessor).process(testStep);
    }

    @Test
    @DisplayName("When 2 parameters extracted from name")
    void testRunTestStepWithParametersFromStepName() throws URISyntaxException {
        initializeTestSteps("test with (.*) and (\\d+)");
        initializeTestStepRunner();

        testStepRunner.run("test with v1 and 2");

        verify(testStepProcessor).process(eq(testStep));
    }

    @Test
    @DisplayName("When 2 parameters extracted from name And additional parameters provided")
    void testRunTestStepWithParametersFromStepNameAndExternally() throws URISyntaxException {
        initializeTestSteps("test with (.*) and (\\d+)");
        initializeTestStepRunner();

        testStepRunner.run("test with v1 and 2", Collections.singletonMap("p1", "v2"));

        verify(testStepProcessor).process(eq(testStep));
    }

    @Test
    @DisplayName("When step doesn't exist Then throw exception")
    void testRunTestStepWhenNotExist() throws URISyntaxException {
        initializeTestSteps("test1");
        initializeTestStepRunner();

        assertThrows(TestStepProcessingException.class, () -> testStepRunner.run("test2"));

        verifyZeroInteractions(testStepProcessor);
    }

    private void initializeTestSteps(String testStepName) {
        testStep = new TestStep.Builder()
                .withName(testStepName)
                .build();

        when(testStepReader.read(any(Path.class)))
                .thenReturn(Stream.of(Collections.<String, Object>emptyMap()).collect(Collectors.toList()));
    }

    private void initializeTestStepRunner() throws URISyntaxException {
        testStepRunner = new DefaultTestStepRunner.Builder()
                .withTestStepProcessor(testStepProcessor)
                .withTestStepReader(testStepReader, Collections.singletonList(Paths.get(getClass().getResource("/steps").toURI())))
                .build();
    }
}