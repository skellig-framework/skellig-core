package org.skellig.connection.channel.factory;


import org.skellig.connection.channel.ReadingChannel;
import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.channel.exception.ChannelConnectionException;
import org.skellig.connection.channel.model.BaseChannelDetails;

public interface ConnectionChannelFactory {

    SendingChannel createSendingChannel(BaseChannelDetails channelDetails)
            throws ChannelConnectionException;

    ReadingChannel createListeningChannel(BaseChannelDetails channelDetails)
            throws ChannelConnectionException;
}
