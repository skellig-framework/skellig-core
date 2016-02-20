package org.skellig.teststep.processor.rmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import org.skellig.teststep.processing.exception.TestStepProcessingException;
import org.skellig.teststep.processor.rmq.model.RmqDetails;
import org.skellig.teststep.processor.rmq.model.RmqExchangeDetails;
import org.skellig.teststep.processor.rmq.model.RmqHostDetails;
import org.skellig.teststep.processor.rmq.model.RmqQueueDetails;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class RmqChannel implements AutoCloseable {

    private Connection conn;
    private Channel channel;
    private RmqDetails rmqDetails;

    RmqChannel(RmqDetails rmqDetails) {
        this.rmqDetails = rmqDetails;
        connectToQueue(createConnectionFactory(rmqDetails));
    }

    void send(Object request, String routingKey) {
        try {
            channel.basicPublish(
                    rmqDetails.getExchange().getName(),
                    routingKey == null ? rmqDetails.getQueue().getRoutingKey() : routingKey,
                    MessageProperties.TEXT_PLAIN,
                    convertRequestToBytes(request)
            );

        } catch (Exception ex) {
            //log later
        }
    }

    Object read(Object acknowledgeResponse, int timeout) {
        final AtomicReference<Object> response = new AtomicReference<>();
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            channel.basicConsume(rmqDetails.getQueue().getName(), true,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope,
                                                   AMQP.BasicProperties properties, byte[] body) {
                            if (response.get() == null) {
                                response.set(body);

                                try {
                                    if (acknowledgeResponse != null) {
                                        sendResponse(properties, acknowledgeResponse);
                                    }
                                } finally {
                                    countDownLatch.countDown();
                                }
                            }
                        }
                    });
            countDownLatch.await(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            //log later
        }
        return response.get();
    }

    boolean isClosed() {
        return !(channel.isOpen() || conn.isOpen());
    }

    private void sendResponse(AMQP.BasicProperties properties, Object message) {
        try {
            channel.basicPublish(
                    "",
                    properties.getReplyTo(),
                    MessageProperties.TEXT_PLAIN,
                    convertRequestToBytes(message)
            );
        } catch (IOException e) {
            // log later
        }
    }

    private byte[] convertRequestToBytes(Object request) {
        byte[] requestAsBytes;
        if (request instanceof byte[]) {
            requestAsBytes = (byte[]) request;
        } else {
            requestAsBytes = String.valueOf(request).getBytes(StandardCharsets.UTF_8);
        }
        return requestAsBytes;
    }

    private ConnectionFactory createConnectionFactory(RmqDetails rmqDetails) {
        RmqHostDetails hostDetails = rmqDetails.getHostDetails();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(hostDetails.getUser());
        factory.setPassword(hostDetails.getPassword());
        factory.setHost(hostDetails.getHost());
        factory.setPort(hostDetails.getPort());

        return factory;
    }

    private void connectToQueue(ConnectionFactory connectionFactory) {

        try {
            conn = connectionFactory.newConnection();
            channel = conn.createChannel();

            RmqExchangeDetails exchange = rmqDetails.getExchange();
            if (exchange.isCreateIfNew()) {
                channel.exchangeDeclare(exchange.getName(),
                        exchange.getType(),
                        exchange.isDurable(),
                        exchange.isAutoDelete(),
                        exchange.getParameters());
            }

            RmqQueueDetails queueDetails = rmqDetails.getQueue();
            if (queueDetails.isCreateIfNew()) {
                channel.queueDeclare(queueDetails.getName(),
                        queueDetails.isDurable(),
                        queueDetails.isExclusive(),
                        queueDetails.isAutoDelete(),
                        queueDetails.getParameters());
            }
            channel.queueBind(queueDetails.getName(), exchange.getName(), queueDetails.getRoutingKey());
        } catch (Exception e) {
            throw new TestStepProcessingException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
            conn.close();
        } catch (Exception e) {
            // log later
        }
    }
}