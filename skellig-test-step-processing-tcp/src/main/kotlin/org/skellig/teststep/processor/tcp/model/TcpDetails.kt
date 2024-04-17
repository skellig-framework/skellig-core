package org.skellig.teststep.processor.tcp.model

/**
 * Represents the details of a TCP connection, including the ID, host name, port, and whether the connection should
 * keep alive.
 *
 * @property id The unique identifier for the TCP connection which represents a channel ID and used in test step to
 * indicate which TCP channel to communicate with.
 * @property hostName The host name or IP address of the TCP server.
 * @property port The port number of the TCP server.
 * @property isKeepAlive Indicates whether the TCP connection should be kept alive.
 */
class TcpDetails(
    val id: String,
    val hostName: String,
    val port: Int,
    val isKeepAlive: Boolean = true
) {

    override fun toString(): String {
        return "$hostName:$port (isKeepAlive = $isKeepAlive)"
    }
}