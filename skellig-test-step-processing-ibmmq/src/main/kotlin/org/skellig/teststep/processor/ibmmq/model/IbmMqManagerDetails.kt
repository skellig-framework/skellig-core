package org.skellig.teststep.processor.ibmmq.model

import java.util.*

class IbmMqManagerDetails private constructor(val name: String,
                                              val channel: String,
                                              val host: String,
                                              val port: Int,
                                              val userCredentials: IbmMqUserCredentials?) {

    class Builder {

        private var host: String? = null
        private var port = 1421
        private var name: String? = null
        private var channel: String? = null
        private var userCredentials: IbmMqUserCredentials? = null

        fun withHost(host: String?) = apply {
            this.host = host
        }

        fun withPort(port: Int) = apply {
            this.port = port
        }

        fun withName(name: String?) = apply {
            this.name = name
        }

        fun withChannel(channel: String?) = apply {
            this.channel = channel
        }

        fun withUserCredentials(userCredentials: IbmMqUserCredentials?) = apply {
            this.userCredentials = userCredentials
        }

        fun build(): IbmMqManagerDetails {
            Objects.requireNonNull(name, "MQ Manager name cannot be null")
            Objects.requireNonNull(channel, "MQ Manager channel cannot be null")
            Objects.requireNonNull(host, "MQ Manager host cannot be null")

            return IbmMqManagerDetails(name!!, channel!!, host!!, port, userCredentials)
        }
    }

    class IbmMqUserCredentials(val username: String, val password: String)
}