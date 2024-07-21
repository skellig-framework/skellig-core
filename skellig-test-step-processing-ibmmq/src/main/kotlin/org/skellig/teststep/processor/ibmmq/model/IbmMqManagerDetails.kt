package org.skellig.teststep.processor.ibmmq.model

/**
 * Represents the details of an IBM MQ Manager.
 *
 * @property name The name of the IBM MQ Manager.
 * @property channel The channel used to connect to the IBM MQ Manager.
 * @property host The host address of the IBM MQ Manager.
 * @property port The port number of the IBM MQ Manager.
 * @property userCredentials The user credentials for authentication with the IBM MQ Manager. Can be null.
 */
class IbmMqManagerDetails private constructor(
    val name: String,
    val channel: String,
    val host: String,
    val port: Int,
    val userCredentials: IbmMqUserCredentials?
) {

    override fun toString(): String {
        return "(name = '$name', channel = '$channel', host = '$host', port = $port${userCredentials?.let { ", userCredentials = $userCredentials" } ?: ""})"
    }

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
            return IbmMqManagerDetails(
                name ?: error("MQ Manager name cannot be null"),
                channel ?: error("MQ Manager channel cannot be null"),
                host ?: error("MQ Manager host cannot be null"),
                port, userCredentials
            )
        }
    }

    class IbmMqUserCredentials(val username: String, val password: String?) {
        override fun toString(): String {
            return "(username = '$username', password = $password)"
        }
    }
}