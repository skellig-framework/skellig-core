package org.skellig.teststep.processor.tcp;

import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.tcp.model.TcpDetails;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

class TcpChannel implements AutoCloseable {

    private static final int DEFAULT_TIMEOUT = 30000;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    TcpChannel(TcpDetails tcpDetails) {
        try {
            socket = new Socket();
            socket.setKeepAlive(tcpDetails.isKeepAlive());
            socket.setSoTimeout(DEFAULT_TIMEOUT);
            socket.connect(new InetSocketAddress(InetAddress.getByName(tcpDetails.getHostName()), tcpDetails.getPort()));

            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new TestStepProcessingException(e.getMessage(), e);
        }
    }

    public void send(Object request) {
        try {
            byte[] messageAsBytes = (byte[]) request;
            outputStream.write(messageAsBytes, 0, messageAsBytes.length);
            outputStream.flush();
        } catch (Exception e) {
            // log later
        }
    }

    public Object read(int timeout, int bufferSize) {
        try {
            if (timeout > 0) {
                socket.setSoTimeout(timeout);
            } else {
                socket.setSoTimeout(DEFAULT_TIMEOUT);
            }
            return readAllBytes(bufferSize);
        } catch (Exception e) {
            //log later
        }
        return null;
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
        } catch (Exception e) {
            //log later
        }
    }

    private byte[] readAllBytes(int bufferSize) throws IOException {
        int read;
        byte[] response = new byte[0];
        byte[] bytes = new byte[bufferSize];
        if ((read = inputStream.read(bytes)) != -1) {
            int shift = response.length;
            response = Arrays.copyOf(response, read + response.length);
            for (int i = shift, j = 0; j < read; i++, j++) {
                response[i] = bytes[j];
            }
        }

        return response.length == 0 ? null : response;
    }

}
