package org.skellig.connection.channel.factory;


import org.skellig.connection.channel.ReadingChannel;
import org.skellig.connection.channel.exception.ChannelConnectionException;
import org.skellig.connection.channel.model.BaseChannelDetails;

public interface ReadingChannelFactory {

    ReadingChannel createReadingChannel(BaseChannelDetails channelDetails)
            throws ChannelConnectionException;
}
