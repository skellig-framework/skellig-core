package org.skellig.teststep.processor.tcp;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.tcp.model.TcpDetails;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;

class TcpChannel implements AutoCloseable {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean isClosed;

    TcpChannel(TcpDetails tcpDetails) {
        try {
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.setSoTimeout(0);
            socket.connect(new InetSocketAddress(InetAddress.getByName(tcpDetails.getHostName()), tcpDetails.getPort()));

            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new TestStepProcessingException(e.getMessage(), e);
        }
    }

    public void send(Object request) {
        if (socket.isConnected()) {
            try {
                byte[] messageAsBytes = (byte[]) request;
                outputStream.write(messageAsBytes, 0, messageAsBytes.length);
                outputStream.flush();
            } catch (Exception e) {
                // log later
            }
        }
    }

    public Optional<Object> read() {
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
    public synchronized void close() {
        try {
            if (socket != null && socket.isConnected()) {
                if (!socket.isClosed()) {
                    inputStream.close();
                    outputStream.close();
                }
                socket.close();
            }
            isClosed = true;
        } catch (Exception e) {
            //log later
        }
    }

    boolean isClosed() {
        return isClosed;
    }
}
