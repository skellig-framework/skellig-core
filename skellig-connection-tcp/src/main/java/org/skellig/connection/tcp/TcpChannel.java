package org.skellig.connection.tcp;

import org.skellig.connection.channel.ReadingChannel;
import org.skellig.connection.channel.SendingChannel;
import org.skellig.connection.channel.exception.ChannelConnectionException;
import org.skellig.connection.tcp.model.TcpDetails;

import java.io.IOException;
import java.util.Optional;

class TcpChannel implements SendingChannel, ReadingChannel {

    private final SocketClient socketClient;
    private boolean isClosed;

    TcpChannel(TcpDetails tcpDetails) throws ChannelConnectionException {
        try {
            this.socketClient = new SocketClient(tcpDetails.getHostName(), tcpDetails.getPort());
        } catch (IOException e) {
            throw new ChannelConnectionException(e);
        }
    }

    @Override
    public Optional<Object> send(Object request) {
        try {
            return socketClient.sendMessage(request);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Object> read() {
        try {
            Optional<Object> response;
            do {
                response = socketClient.readMessage();
                Thread.sleep(1);
            } while (!response.isPresent() && !isClosed);

            return response;
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public synchronized void close() {
        try {
            socketClient.close();
            isClosed = true;
        } catch (Exception e) {
            //log later
        }
    }

    boolean isClosed() {
        return isClosed;
    }
}
