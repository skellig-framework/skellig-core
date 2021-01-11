package org.skellig.teststep.processor.rmq.model

class RmqDetails private constructor(val channelId: String,
                                     val hostDetails: RmqHostDetails,
                                     val exchange: RmqExchangeDetails,
                                     val queue: RmqQueueDetails) {

    class Builder {

        private var channelId: String? = null
        private var hostDetails: RmqHostDetails? = null
        private var exchange: RmqExchangeDetails? = null
        private var queue: RmqQueueDetails? = null

        fun withChannelId(channelId: String?) = apply {
            this.channelId = channelId
        }

        fun withHostDetails(hostDetails: RmqHostDetails?) = apply {
            this.hostDetails = hostDetails
        }

        fun withExchange(exchange: RmqExchangeDetails?) = apply {
            this.exchange = exchange
        }

        fun withQueue(queue: RmqQueueDetails?) = apply {
            this.queue = queue
        }

        fun build(): RmqDetails {
            return RmqDetails(channelId!!, hostDetails!!, exchange!!, queue!!)
        }
    }
}