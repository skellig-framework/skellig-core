package org.skellig.teststep.processor.rmq.model

/**
 * Represents the details of an RMQ host.
 *
 * @property host The host address of the RMQ server.
 * @property port The port number of the RMQ server.
 * @property user The username for authentication. Can be null if authentication is not required.
 * @property password The password for authentication. Can be null if authentication is not required.
 */
class RmqHostDetails(
    val host: String,
    val port: Int,
    val user: String?,
    val password: String?
) {

    override fun toString(): String {
        return "$host:$port"
    }

}