package org.skellig.teststep.processor.tcp;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
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

public class TcpTestStepProcessor extends BaseTestStepProcessor<TcpTestStep> {

    private Map<String, TcpChannel> tcpChannels;

    private TcpTestStepProcessor(Map<String, TcpChannel> tcpChannels,
                                 TestScenarioState testScenarioState,
                                 TestStepResultValidator validator,
                                 TestStepResultConverter testStepResultConverter) {
        super(testScenarioState, validator, testStepResultConverter);
        this.tcpChannels = tcpChannels;
    }

    @Override
    protected Object processTestStep(TcpTestStep testStep) {
        Object response = null;
        Optional<String> sendTo = testStep.getSendTo();
        Optional<String> receiveFrom = testStep.getReceiveFrom();
        Optional<String> respondTo = testStep.getReceiveFrom();

        sendTo.ifPresent(channel -> send(testStep.getTestData(), channel));

        if (receiveFrom.isPresent()) {
            TcpChannel tcpChannel = tcpChannels.get(receiveFrom.get());
            response = runTask(tcpChannel::read, r -> r.isPresent() || tcpChannel.isClosed(), 5, 30).orElse(null);

            respondTo.ifPresent(s -> send(testStep.getTestData(), s));
        }
        return response;
    }

    private void send(Object testData, String channel) {
        TcpChannel tcpChannel = tcpChannels.get(channel);
        tcpChannel.send(testData);
    }

    @Override
    public Class<TcpTestStep> getTestStepClass() {
        return TcpTestStep.class;
    }

    @Override
    public void close() {
        tcpChannels.values().forEach(TcpChannel::close);
    }

    public static class Builder extends BaseTestStepProcessor.Builder<TcpTestStep> {

        private static final String TCP_CONFIG_KEYWORD = "tcp";

        private Map<String, TcpChannel> tcpChannels;

        public Builder() {
            tcpChannels = new HashMap<>();
        }

        public Builder withTcpChannel(TcpDetails tcpDetails) {
            this.tcpChannels.putIfAbsent(tcpDetails.getChannelId(), new TcpChannel(tcpDetails));
            return this;
        }

        public Builder withTcpChannels(Config config) {
            if (config.hasPath(TCP_CONFIG_KEYWORD)) {
                ((List<Map<String, String>>) config.getAnyRefList(TCP_CONFIG_KEYWORD))
                        .forEach(rawHttpService ->
                                withTcpChannel(new TcpDetails(rawHttpService.get("channelId"),
                                        rawHttpService.get("host"), Integer.parseInt(rawHttpService.get("port")))));
            }
            return this;
        }

        public TestStepProcessor<TcpTestStep> build() {
            return new TcpTestStepProcessor(tcpChannels, testScenarioState, validator, testStepResultConverter);
        }
    }

}
