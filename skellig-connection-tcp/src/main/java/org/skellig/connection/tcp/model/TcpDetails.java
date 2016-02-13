package org.skellig.connection.tcp.model;

import org.skellig.connection.channel.model.BaseChannelDetails;

public class TcpDetails extends BaseChannelDetails {

    private String hostName;
    private int port;

    public TcpDetails(String channelId, String hostName, int port) {
        super(channelId);
        this.hostName = hostName;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

}
