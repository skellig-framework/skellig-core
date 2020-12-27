package org.skellig.teststep.processor.tcp;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.tcp.model.TcpDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TcpChannelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpChannelTest.class);
    private static final String DEFAULT_DATA = StringUtils.repeat("a", 512);
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private Collection<SocketRequestHandler> socketRequestHandlers;
    private TcpChannel tcpChannel;
    private ExecutorService executorService;
    private ServerSocket server;

    @BeforeEach
    void setUp() {
        socketRequestHandlers = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() throws IOException {
        executorService.shutdown();
        tcpChannel.close();
        socketRequestHandlers.forEach(SocketRequestHandler::close);
        server.close();

    }

    @Test
    @DisplayName("Send and read When Server get request and responds only Then received response")
    void testSendAndReadTwoTimes() throws InterruptedException {
        startSocketServer();

        tcpChannel.send(DEFAULT_DATA.getBytes());
        Object response = tcpChannel.read(1000, DEFAULT_BUFFER_SIZE);

        assertEquals(DEFAULT_DATA, new String((byte[]) response));

        tcpChannel.send(DEFAULT_DATA.getBytes());
        response = tcpChannel.read(1000, DEFAULT_BUFFER_SIZE);

        assertEquals(DEFAULT_DATA, new String((byte[]) response));
    }

    @Test
    @DisplayName("Read once When Server responds only once Then response received")
    void testRead() throws InterruptedException {
        startSocketServer(0, 1);

        Object response = tcpChannel.read(4000, DEFAULT_BUFFER_SIZE);

        assertEquals(DEFAULT_DATA, new String((byte[]) response));
    }

    @Test
    @DisplayName("Read once When times out Then response not received")
    void testReadWhenTimedOut() throws InterruptedException {
        startSocketServer(200, 1);

        Object response = tcpChannel.read(100, DEFAULT_BUFFER_SIZE);

        assertNull(response);
    }

    @Test
    @DisplayName("Read 2 times When first read times out Then last read receives response")
    void testReadSeveralTimesWhenTimedOut() throws InterruptedException {
        startSocketServer(200, 1);

        Object response = tcpChannel.read(100, DEFAULT_BUFFER_SIZE);
        assertNull(response);

        response = tcpChannel.read(500, DEFAULT_BUFFER_SIZE);
        assertEquals(DEFAULT_DATA, new String((byte[]) response));
    }

    @Test
    @DisplayName("Read 2 times When Server responds only once Then last read times out")
    void testReadSeveralTimesWhenServerRespondedOnce() throws InterruptedException {
        startSocketServer(0, 1);

        Object response = tcpChannel.read(0, DEFAULT_BUFFER_SIZE);
        assertNotNull(response);

        response = tcpChannel.read(100, DEFAULT_BUFFER_SIZE);
        assertNull(response);
    }

    @Test
    @DisplayName("Read 3 times with different timeouts When Server responds only 2 times with delay " +
            "Then verify receives 2 responses")
    void testReadSeveralTimesWhenServerRespondedTwiceAndDelay() throws InterruptedException {
        startSocketServer(300, 2);

        Object response = tcpChannel.read(0, DEFAULT_BUFFER_SIZE);
        assertNotNull(response);

        response = tcpChannel.read(100, DEFAULT_BUFFER_SIZE);
        assertNull(response);

        response = tcpChannel.read(500, DEFAULT_BUFFER_SIZE);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Read once When Server responds only once Then response received")
    void testReadLargeDataAndMaxBufferIsLimited() throws InterruptedException {
        startSocketServer(0, 1);

        Object response = tcpChannel.read(100, 32);

        assertEquals(32, new String((byte[]) response).length());
    }


    private void startSocketServer() throws InterruptedException {
        startSocketServer(0, 0);
    }

    private void startSocketServer(int delay, int respondTimes) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        executorService.execute(() -> {
            try {
                server = new ServerSocket(1116);
                countDownLatch.countDown();
                while (!executorService.isShutdown()) {
                    try {
                        Socket socket = server.accept();
                        executorService.submit(() -> {
                            SocketRequestHandler socketRequestHandler = new SocketRequestHandler(socket, delay, respondTimes);
                            socketRequestHandlers.add(socketRequestHandler);
                            socketRequestHandler.run();
                        });
                    } catch (Exception ignored) {
                    }
                }
                LOGGER.debug("Shutting tcp server down...");
            } catch (Exception ignored) {
            }
        });
        // wait a bit to startup the server
        countDownLatch.await(2, TimeUnit.SECONDS);

        tcpChannel = new TcpChannel(new TcpDetails("h1", "localhost", 1116));
    }

    private class SocketRequestHandler implements Runnable {

        private Socket socket;
        private int delay;
        private int respondTimes;
        private boolean isRespondOnly;

        public SocketRequestHandler(Socket socket, int delay, int respondTimes) {
            this.socket = socket;
            this.delay = delay;
            this.respondTimes = respondTimes;
            isRespondOnly = respondTimes > 0;
        }

        public void run() {
            try (DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                LOGGER.debug("Connected to " + socket.getRemoteSocketAddress());
                System.out.println("Connected to " + socket.getRemoteSocketAddress());

                readAndRespond(in, out);
            } catch (Exception ex) {
                close();
            }
        }

        private void readAndRespond(DataInputStream in, DataOutputStream out) throws Exception {
            while (!socket.isClosed() && !executorService.isShutdown() && respondTimes >= 0) {
                if (isRespondOnly) {
                    if (respondTimes-- > 0) {
                        respond(out, DEFAULT_DATA.getBytes());
                    }

                } else {
                    byte[] bytes = new byte[DEFAULT_DATA.length()];
                    int read = in.read(bytes);
                    bytes = Arrays.copyOf(bytes, read);
                    LOGGER.debug("Data read: " + new String(bytes));

                    respond(out, bytes);
                }
            }
        }

        private void respond(DataOutputStream out, byte[] bytes) throws InterruptedException, IOException {
            if (delay > 0) {
                Thread.sleep(delay);
            }

            out.write(bytes, 0, bytes.length);
            out.flush();
            LOGGER.debug("Sent data: " + new String(bytes));
        }

        public void close() {
            try {
                socket.close();
            } catch (Exception ignored) {

            }
        }
    }
}