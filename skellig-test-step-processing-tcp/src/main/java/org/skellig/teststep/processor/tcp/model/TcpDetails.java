package org.skellig.teststep.processor.tcp.model;

import java.util.Objects;

public class TcpDetails {

    private String channelId;
    private String hostName;
    private int port;
    private boolean keepAlive;

    public TcpDetails(String channelId, String hostName, int port) {
        this(channelId, hostName, port, true);
    }

    public TcpDetails(String channelId, String hostName, int port, boolean keepAlive) {
        this.keepAlive = keepAlive;
        Objects.requireNonNull(channelId, "TCP Channel ID must not be null and " +
                "have a name of a registered TCP Channel in your test configuration");
        Objects.requireNonNull(hostName, "Host name of TCP must not be null (ex. localhost, 192.168.0.10, etc.)");

        this.channelId = channelId;
        this.hostName = hostName;
        this.port = port;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }
}
