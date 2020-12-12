package org.skellig.teststep.processor.unix;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails;
import org.skellig.teststep.processor.unix.model.UnixShellTestStep;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UnixShellTestStepProcessor extends BaseTestStepProcessor<UnixShellTestStep> {

    private Map<String, DefaultSshClient> hosts;

    protected UnixShellTestStepProcessor(TestScenarioState testScenarioState,
                                         TestStepResultValidator validator,
                                         TestStepResultConverter testStepResultConverter,
                                         Map<String, DefaultSshClient> hosts) {
        super(testScenarioState, validator, testStepResultConverter);
        this.hosts = hosts;
    }

    @Override
    protected Object processTestStep(UnixShellTestStep testStep) {
        if (testStep.getHosts().isEmpty()) {
            throw new TestStepProcessingException("No hosts were provided to run a command." +
                    " Registered hosts are: " + hosts.keySet().toString());
        }

        return testStep.getHosts().parallelStream()
                .collect(Collectors.toMap(host -> host,
                        host -> {
                            DefaultSshClient sshClient = getDefaultSshClient(host);
                            return sshClient.runShellCommand(testStep.getCommand(), testStep.getTimeout());
                        }));
    }

    private DefaultSshClient getDefaultSshClient(String host) {
        if (!hosts.containsKey(host)) {
            throw new TestStepProcessingException(String.format("No hosts was registered for host name '%s'." +
                    " Registered hosts are: %s", host, hosts.keySet().toString()));
        }
        return hosts.get(host);
    }

    @Override
    public Class<UnixShellTestStep> getTestStepClass() {
        return UnixShellTestStep.class;
    }

    public static class Builder extends BaseTestStepProcessor.Builder<UnixShellTestStep> {

        private Map<String, DefaultSshClient> hosts;
        private UnixShellConfigReader unixShellConfigReader;

        public Builder() {
            hosts = new HashMap<>();
            unixShellConfigReader = new UnixShellConfigReader();
        }

        public Builder withHost(UnixShellHostDetails unixShellHostDetails) {
            this.hosts.put(unixShellHostDetails.getHostName(),
                    new DefaultSshClient.Builder()
                            .withHost(unixShellHostDetails.getHostAddress())
                            .withPort(unixShellHostDetails.getPort())
                            .withUser(unixShellHostDetails.getUserName())
                            .withPassword(unixShellHostDetails.getPassword())
                            .withPassword(unixShellHostDetails.getSshKeyPath())
                            .build());
            return this;
        }

        public Builder withHost(Config config) {
            unixShellConfigReader.read(config).forEach(this::withHost);
            return this;
        }

        @Override
        public TestStepProcessor<UnixShellTestStep> build() {
            return new UnixShellTestStepProcessor(testScenarioState, validator, testStepResultConverter, hosts);
        }
    }
}
