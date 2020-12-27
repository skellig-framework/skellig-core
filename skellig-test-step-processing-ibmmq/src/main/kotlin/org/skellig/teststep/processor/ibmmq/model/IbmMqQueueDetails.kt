package org.skellig.teststep.processor.ibmmq.model

import java.util.*

class IbmMqQueueDetails private constructor(val channelId: String,
                                            val queueName: String,
                                            val ibmMqManagerDetails: IbmMqManagerDetails) {

    class Builder {
        private var channelId: String? = null
        private var queueName: String? = null
        private var ibmMqManagerDetails: IbmMqManagerDetails? = null

        fun withQueueName(queueName: String?) = apply {
            this.queueName = queueName
        }

        fun withChannelId(channelId: String?) = apply {
            this.channelId = channelId
        }

        fun withMqManagerDetails(ibmMqManagerDetails: IbmMqManagerDetails?) = apply {
            this.ibmMqManagerDetails = ibmMqManagerDetails
        }

        fun build(): IbmMqQueueDetails {
            Objects.requireNonNull(channelId, "Channel Id cannot be null")
            Objects.requireNonNull(queueName, "Queue name cannot be null")
            Objects.requireNonNull(ibmMqManagerDetails, "Queue manager cannot be null")

            return IbmMqQueueDetails(channelId!!, queueName!!, ibmMqManagerDetails!!)
        }
    }
}