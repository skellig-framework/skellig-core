package org.skellig.teststep.processor.unix;

import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
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
        Map<String, String> results =
                hosts.entrySet().parallelStream()
                        .filter(host -> testStep.getHosts().isEmpty() || testStep.getHosts().contains(host.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                entry -> entry.getValue().runShellCommand(testStep.getCommand(), testStep.getTimeout())));

        if (results.isEmpty()) {
            throw new TestStepProcessingException(
                    String.format("Cannot find hosts '%s' in registered hosts '%s'", testStep.getHosts(), hosts.keySet()));
        }
        return results;
    }

    @Override
    public Class<UnixShellTestStep> getTestStepClass() {
        return UnixShellTestStep.class;
    }

    public static class Builder extends BaseTestStepProcessor.Builder<UnixShellTestStep> {

        private Map<String, DefaultSshClient> hosts;

        public Builder() {
            hosts = new HashMap<>();
        }

        public Builder withHost(String name, String host, int port, String userName, String password) {
            this.hosts.put(name,
                    new DefaultSshClient.Builder()
                            .withHost(host)
                            .withPort(port)
                            .withUser(userName)
                            .withPassword(password)
                            .build());
            return this;
        }

        public Builder withHost(String name, String user, String sshKeyPath, String host, int port) {
            this.hosts.put(name,
                    new DefaultSshClient.Builder()
                            .withHost(host)
                            .withPort(port)
                            .withUser(user)
                            .withPrivateSshKeyPath(sshKeyPath)
                            .build());
            return this;
        }

        @Override
        public TestStepProcessor<UnixShellTestStep> build() {
            return new UnixShellTestStepProcessor(testScenarioState, validator, testStepResultConverter, hosts);
        }
    }
}
