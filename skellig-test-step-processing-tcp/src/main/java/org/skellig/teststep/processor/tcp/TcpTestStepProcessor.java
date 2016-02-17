package org.skellig.teststep.processor.tcp;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.processor.BaseTestStepProcessor;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.tcp.model.TcpDetails;
import org.skellig.teststep.processor.tcp.model.TcpTestStep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.skellig.task.TaskUtils.runTask;
import static org.skellig.task.async.AsyncTaskUtils.runTaskAsync;

public class TcpTestStepProcessor extends BaseTestStepProcessor<TcpTestStep> {

    private Map<String, TcpChannel> tcpChannels;

    private TcpTestStepProcessor(Map<String, TcpChannel> tcpChannels,
                                 TestScenarioState testScenarioState,
                                 TestStepResultValidator validator) {
        super(testScenarioState, validator);
        this.tcpChannels = tcpChannels;
    }

    @Override
    public void process(TcpTestStep testStep) {
        runTaskAsync(() -> {
                    super.process(testStep);
                    return Optional.empty();
                }
        );
    }

    @Override
    protected Object processTestStep(TcpTestStep testStep) {
        Object response = null;
        Optional<String> sendTo = testStep.getSendTo();
        Optional<String> readFrom = testStep.getReadFrom();

        if (sendTo.isPresent()) {
            TcpChannel tcpChannel = tcpChannels.get(sendTo.get());
            tcpChannel.send(testStep.getTestData());
        } else if (readFrom.isPresent()) {
            TcpChannel tcpChannel = tcpChannels.get(readFrom.get());
            response = runTask(tcpChannel::read, r -> r.isPresent() || tcpChannel.isClosed(), 5, 30).orElse(null);
        }
        return response;
    }

    @Override
    public Class<TcpTestStep> getTestStepClass() {
        return TcpTestStep.class;
    }

    @Override
    public void close() {
        tcpChannels.values().forEach(TcpChannel::close);
    }

    public static class Builder {

        private static final String TCP_CONFIG_KEYWORD = "tcp";

        private Map<String, TcpChannel> tcpChannels;
        private TestScenarioState testScenarioState;
        private TestStepResultValidator validator;

        public Builder() {
            tcpChannels = new HashMap<>();
        }

        public Builder withTcpChannel(TcpDetails tcpDetails) {
            this.tcpChannels.putIfAbsent(tcpDetails.getChannelId(), new TcpChannel(tcpDetails));
            return this;
        }

        public Builder withTcpChannel(Config config) {
            if (config.hasPath(TCP_CONFIG_KEYWORD)) {
                ((List<Map<String, String>>) config.getAnyRefList(TCP_CONFIG_KEYWORD))
                        .forEach(rawHttpService ->
                                withTcpChannel(new TcpDetails(rawHttpService.get("channelId"),
                                        rawHttpService.get("host"), Integer.parseInt(rawHttpService.get("port")))));
            }
            return this;
        }

        public Builder withTestScenarioState(TestScenarioState testScenarioState) {
            this.testScenarioState = testScenarioState;
            return this;
        }

        public Builder withValidator(TestStepResultValidator validator) {
            this.validator = validator;
            return this;
        }

        public TestStepProcessor<TcpTestStep> build() {
            return new TcpTestStepProcessor(tcpChannels, testScenarioState, validator);
        }
    }

}