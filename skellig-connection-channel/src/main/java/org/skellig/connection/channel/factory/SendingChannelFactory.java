package org.skellig.connection.channel.factory;


import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.channel.exception.ChannelConnectionException;
import org.skellig.connection.channel.model.BaseChannelDetails;

public interface SendingChannelFactory {

    SendingChannel createSendingChannel(BaseChannelDetails channelDetails)
            throws ChannelConnectionException;
}
