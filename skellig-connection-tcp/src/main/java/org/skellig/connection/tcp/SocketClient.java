package org.skellig.connection.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;


class SocketClient implements AutoCloseable {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    SocketClient(String hostname, int port) throws IOException {
        socket = new Socket();
        socket.setKeepAlive(true);
        socket.setSoTimeout(0);
        socket.connect(new InetSocketAddress(InetAddress.getByName(hostname), port));

        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }


    Optional<Object> sendMessage(Object message) {
        Optional<Object> result = Optional.empty();
        if (socket.isConnected()) {
            try {
                byte[] messageAsBytes = (byte[]) message;
                outputStream.write(messageAsBytes, 0, messageAsBytes.length);
                outputStream.flush();
                result = Optional.empty();
            } catch (Exception e) {
                // log later
            }
        }
        return result;
    }

    Optional<Object> readMessage() {
        try {
            if (inputStream.available() > 0) {
                byte[] bytes = new byte[inputStream.available()];
                inputStream.readFully(bytes, 0, bytes.length);
                return Optional.of(bytes);
            }
        } catch (Exception e) {
            //log later
        }
        return Optional.empty();
    }

    @Override
    public synchronized void close() throws IOException {
        if (socket != null && socket.isConnected()) {
            if (!socket.isClosed()) {
                inputStream.close();
                outputStream.close();
            }
            socket.close();
        }
    }
}