package org.skellig.teststep.processor.tcp;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
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

public class TcpTestStepProcessor extends BaseTestStepProcessor<TcpTestStep> {

    private Map<String, TcpChannel> tcpChannels;

    protected TcpTestStepProcessor(Map<String, TcpChannel> tcpChannels,
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
        Optional<String> respondTo = testStep.getRespondTo();

        sendTo.ifPresent(channel -> send(testStep.getTestData(), channel));

        if (receiveFrom.isPresent()) {
            TcpChannel tcpChannel = tcpChannels.get(receiveFrom.get());
            response = tcpChannel.read(testStep.getTimeout(), testStep.getReadBufferSize());
            validate(testStep, response);

            respondTo.ifPresent(c -> send(testStep.getTestData(), c));
        }
        return response;
    }

    private void send(Object testData, String channel) {
        if (tcpChannels.containsKey(channel)) {
            tcpChannels.get(channel).send(testData);
        } else {
            throw new TestStepProcessingException(String.format("Channel '%s' was not registered " +
                    "in TCP Test Step Processor", channel));
        }
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
        public static final String HOST = "host";
        public static final String CHANNEL_ID = "channelId";
        public static final String PORT = "port";
        public static final String KEEP_ALIVE = "keepAlive";

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
                        .forEach(item -> {
                            try {
                                int port = Integer.parseInt(item.get(PORT));
                                boolean keepAlive = item.containsKey(KEEP_ALIVE) ? Boolean.parseBoolean(item.get(KEEP_ALIVE)) : true;
                                withTcpChannel(new TcpDetails(item.get(CHANNEL_ID), item.get(HOST), port, keepAlive));
                            } catch (NumberFormatException e) {
                                throw new NumberFormatException("Invalid number assigned to TCP port in configuration");
                            }
                        });
            }
            return this;
        }

        public TestStepProcessor<TcpTestStep> build() {
            return new TcpTestStepProcessor(tcpChannels, testScenarioState, validator, testStepResultConverter);
        }
    }

}
