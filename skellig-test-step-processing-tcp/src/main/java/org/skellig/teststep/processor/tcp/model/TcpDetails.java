package org.skellig.teststep.processor.tcp.model;

public class TcpDetails {

    private String channelId;
    private String hostName;
    private int port;

    public TcpDetails(String channelId, String hostName, int port) {
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

}
