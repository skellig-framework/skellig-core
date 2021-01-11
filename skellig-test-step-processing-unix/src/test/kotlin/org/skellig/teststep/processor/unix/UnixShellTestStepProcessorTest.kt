package org.skellig.teststep.processor.unix;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.unix.model.UnixShellTestStep;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class UnixShellTestStepProcessorTest {

    private UnixShellTestStepProcessor processor;
    private DefaultSshClient sshClient;
    private DefaultSshClient sshClient2;

    @BeforeEach
    void setUp() {
        sshClient = mock(DefaultSshClient.class);
        sshClient2 = mock(DefaultSshClient.class);

        Map<String, DefaultSshClient> hosts = new HashMap<>();
        hosts.put("h1", sshClient);
        hosts.put("h2", sshClient2);

        processor = new UnixShellTestStepProcessor(mock(TestScenarioState.class),
                mock(TestStepResultValidator.class), mock(TestStepResultConverter.class), hosts);
    }

    @Test
    void testRunCommandToNoHost() {
        UnixShellTestStep testStep =
                new UnixShellTestStep.Builder()
                        .withCommand("ls")
                        .build();

        processor.process(testStep)
                .subscribe((t, r, e) -> {
                    assertEquals("No hosts were provided to run a command." +
                            " Registered hosts are: [h1, h2]", e.getMessage());
                });
    }

    @Test
    void testRunCommandToNonRegisteredHost() {
        UnixShellTestStep testStep =
                new UnixShellTestStep.Builder()
                        .withHosts(Collections.singletonList("h3"))
                        .withCommand("ls")
                        .build();

        processor.process(testStep)
                .subscribe((t, r, e) -> {
                    assertEquals("No hosts was registered for host name 'h3'." +
                            " Registered hosts are: [h1, h2]", e.getMessage());
                });
    }

    @Test
    void testRunCommand() {
        UnixShellTestStep testStep =
                new UnixShellTestStep.Builder()
                        .withHosts(Collections.singletonList("h1"))
                        .withCommand("ls -l")
                        .build();

        when(sshClient.runShellCommand(testStep.getCommand(), testStep.getTimeout())).thenReturn("r1");

        processor.process(testStep)
                .subscribe((t, r, e) -> {
                    Map<String, String> result = (Map<String, String>) r;
                    assertEquals(1, result.size());
                    assertEquals("r1", result.get("h1"));
                });

        verifyZeroInteractions(sshClient2);
    }

    @Test
    void testRunCommandWithArguments() {
        Map<String, String> args = new HashMap<>();
        args.put("a1", "v1");
        args.put("a2", "v2");

        UnixShellTestStep testStep =
                new UnixShellTestStep.Builder()
                        .withHosts(Arrays.asList("h1", "h2"))
                        .withCommand("cmd1")
                        .withArgs(args)
                        .build();

        when(sshClient.runShellCommand(testStep.getCommand(), testStep.getTimeout())).thenReturn("r1");
        when(sshClient2.runShellCommand(testStep.getCommand(), testStep.getTimeout())).thenReturn("r2");

        processor.process(testStep)
                .subscribe((t, r, e) -> {
                    Map<String, String> result = (Map<String, String>) r;
                    assertEquals("r1", result.get("h1"));
                    assertEquals("r2", result.get("h2"));
                });
    }

}