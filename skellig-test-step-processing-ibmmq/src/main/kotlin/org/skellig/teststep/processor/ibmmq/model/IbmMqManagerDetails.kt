package org.skellig.teststep.processor.ibmmq.model

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

        fun host(host: String?) = apply {
            this.host = host
        }

        fun port(port: Int) = apply {
            this.port = port
        }

        fun name(name: String?) = apply {
            this.name = name
        }

        fun channel(channel: String?) = apply {
            this.channel = channel
        }

        fun userCredentials(userCredentials: IbmMqUserCredentials?) = apply {
            this.userCredentials = userCredentials
        }

        fun build(): IbmMqManagerDetails {
            return IbmMqManagerDetails(name ?: error("MQ Manager name cannot be null"),
                    channel ?: error("MQ Manager channel cannot be null"),
                    host ?: error("MQ Manager host cannot be null"),
                    port, userCredentials)
        }
    }

    class IbmMqUserCredentials(val username: String, val password: String?)
}