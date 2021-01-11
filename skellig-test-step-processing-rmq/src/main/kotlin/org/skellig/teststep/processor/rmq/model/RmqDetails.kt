package org.skellig.teststep.processor.rmq.model;


public class RmqDetails {

    private String channelId;
    private RmqHostDetails hostDetails;
    private RmqExchangeDetails exchange;
    private RmqQueueDetails queue;

    private RmqDetails(String channelId, RmqHostDetails hostDetails, RmqExchangeDetails exchange, RmqQueueDetails queue) {
        this.channelId = channelId;
        this.hostDetails = hostDetails;
        this.exchange = exchange;
        this.queue = queue;
    }

    public String getChannelId() {
        return channelId;
    }

    public RmqHostDetails getHostDetails() {
        return hostDetails;
    }

    public RmqExchangeDetails getExchange() {
        return exchange;
    }

    public RmqQueueDetails getQueue() {
        return queue;
    }

    public static class Builder {

        private String channelId;
        private RmqHostDetails hostDetails;
        private RmqExchangeDetails exchange;
        private RmqQueueDetails queue;

        public Builder withChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder withHostDetails(RmqHostDetails hostDetails) {
            this.hostDetails = hostDetails;
            return this;
        }

        public Builder withExchange(RmqExchangeDetails exchange) {
            this.exchange = exchange;
            return this;
        }

        public Builder withQueue(RmqQueueDetails queue) {
            this.queue = queue;
            return this;
        }

        public RmqDetails build() {
            return new RmqDetails(channelId, hostDetails, exchange, queue);
        }
    }
}
