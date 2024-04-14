package org.skellig.teststep.processor.tcp.model

class TcpDetails(val id: String,
                 val hostName: String,
                 val port: Int,
                 val isKeepAlive: Boolean = true) {

    override fun toString(): String {
        return "$hostName:$port (isKeepAlive = $isKeepAlive)"
    }
}