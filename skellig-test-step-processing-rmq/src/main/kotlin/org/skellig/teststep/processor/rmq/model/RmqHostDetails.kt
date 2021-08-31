package org.skellig.teststep.processor.rmq.model

class RmqHostDetails(val host: String,
                     val port: Int,
                     val user: String?,
                     val password: String?) {

    override fun toString(): String {
        return "$host:$port"
    }

    class Builder {

        private var host: String? = null
        private var port = 5672
        private var user: String? = null
        private var password: String? = null

        fun withHost(host: String?) = apply {
            this.host = host
        }

        fun withPort(port: Int) = apply {
            this.port = port
        }

        fun withUser(user: String?) = apply {
            this.user = user
        }

        fun withPassword(password: String?) = apply {
            this.password = password
        }

        fun build(): RmqHostDetails {
            return RmqHostDetails(host!!, port, user, password)
        }
    }

}