package org.skellig.connection.tcp;


import org.skellig.connection.channel.ReadingChannel;
import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.channel.exception.ChannelConnectionException;
import org.skellig.connection.channel.factory.ReadingChannelFactory;
import org.skellig.connection.channel.factory.SendingChannelFactory;
import org.skellig.connection.channel.model.BaseChannelDetails;
import org.skellig.connection.tcp.model.TcpDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TcpChannelFactory implements SendingChannelFactory, ReadingChannelFactory {

    private Map<String, TcpChannel> channels;

    public TcpChannelFactory() {
        channels = new HashMap<>();
    }

    @Override
    public synchronized SendingChannel createSendingChannel(BaseChannelDetails channelDetails) throws ChannelConnectionException {
        return getTcpChannelFor(channelDetails);
    }

    @Override
    public synchronized ReadingChannel createReadingChannel(BaseChannelDetails channelDetails) throws ChannelConnectionException {
        return getTcpChannelFor(channelDetails);
    }

    private TcpChannel getTcpChannelFor(BaseChannelDetails channelDetails) {
        Objects.requireNonNull(channelDetails, "TCP Channel details cannot be null");

        if (!channels.containsKey(channelDetails.getChannelId())) {
            if (channelDetails instanceof TcpDetails) {
                registerNewTcpChannelFor(channelDetails);
            } else {
                throw new ChannelConnectionException("Can't register TCP channel for " + channelDetails.getClass() +
                        ". TcpDetails is supported only");
            }
        } else if (channels.get(channelDetails.getChannelId()).isClosed()) {
            registerNewTcpChannelFor(channelDetails);
        }
        return channels.get(channelDetails.getChannelId());
    }

    private void registerNewTcpChannelFor(BaseChannelDetails channelDetails) {
        channels.put(channelDetails.getChannelId(), new TcpChannel((TcpDetails) channelDetails));
    }
}
