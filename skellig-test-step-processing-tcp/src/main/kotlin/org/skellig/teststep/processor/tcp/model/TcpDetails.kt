package org.skellig.teststep.processor.tcp.model

class TcpDetails(val channelId: String,
                 val hostName: String,
                 val port: Int,
                 val isKeepAlive: Boolean = true)