package org.skellig.teststep.processor.ibmmq.model;

import java.util.Objects;

public class IbmMqQueueDetails {

    private String channelId;
    private String queueName;
    private IbmMqManagerDetails ibmMqManagerDetails;

    private IbmMqQueueDetails(String channelId, String queueName, IbmMqManagerDetails ibmMqManagerDetails) {
        Objects.requireNonNull(channelId, "Channel Id cannot be null");
        Objects.requireNonNull(queueName, "Queue name cannot be null");
        Objects.requireNonNull(ibmMqManagerDetails, "Queue manager cannot be null");

        this.channelId = channelId;
        this.queueName = queueName;
        this.ibmMqManagerDetails = ibmMqManagerDetails;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getQueueName() {
        return queueName;
    }

    public IbmMqManagerDetails getIbmMqManagerDetails() {
        return ibmMqManagerDetails;
    }

    public static class Builder {

        private String channelId;
        private String queueName;
        private IbmMqManagerDetails ibmMqManagerDetails;

        public Builder withQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public Builder withChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder withMqManagerDetails(IbmMqManagerDetails ibmMqManagerDetails) {
            this.ibmMqManagerDetails = ibmMqManagerDetails;
            return this;
        }

        public IbmMqQueueDetails build() {
            return new IbmMqQueueDetails(channelId, queueName, ibmMqManagerDetails);
        }
    }

}
