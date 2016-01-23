package org.skellig.connection.channel.model;

import java.util.Objects;

public abstract class BaseChannelDetails {

    protected String channelId;

    public BaseChannelDetails(String channelId) {
        Objects.requireNonNull(channelId, "Channel ID must be set");
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }
}
