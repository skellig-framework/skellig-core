package org.skellig.teststep.processor.unix.model;

import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.TestStepExecutionType;
import org.skellig.teststep.processing.model.ValidationDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class UnixShellTestStep extends TestStep {

    private Collection<String> hosts;
    private String user;
    private String password;
    private String command;
    private Map<String, String> args;

    protected UnixShellTestStep(String id, String name, TestStepExecutionType execution,
                                int timeout, int delay, Map<String, Object> variables,
                                Object testData, ValidationDetails validationDetails,
                                Collection<String> hosts, String command, Map<String, String> args) {
        super(id, name, execution, timeout, delay, variables, testData, validationDetails);
        this.hosts = hosts;
        this.command = command;
        this.args = args;
    }

    public Collection<String> getHosts() {
        return hosts;
    }

    public String getCommand() {
        return args == null ? command :
                command + " " + args.entrySet().stream()
                        .map(entry -> "-" + entry.getKey() + " " + entry.getValue())
                        .collect(Collectors.joining(" "));
    }

    public static class Builder extends TestStep.Builder {

        private static final int DEFAULT_TIMEOUT = 30000;

        private Collection<String> hosts;
        private String command;
        private Map<String, String> args;

        public Builder() {
            hosts = Collections.emptyList();
            timeout = DEFAULT_TIMEOUT;
        }

        public Builder withHosts(Collection<String> hosts) {
            this.hosts = hosts;
            return this;
        }

        public Builder withCommand(String command) {
            this.command = command;
            return this;
        }

        public Builder withArgs(Map<String, String> args) {
            this.args = args;
            return this;
        }

        public UnixShellTestStep build() {
            return new UnixShellTestStep(id, name, execution, timeout, delay, variables, testData, validationDetails,
                    hosts, command, args);
        }
    }
}
