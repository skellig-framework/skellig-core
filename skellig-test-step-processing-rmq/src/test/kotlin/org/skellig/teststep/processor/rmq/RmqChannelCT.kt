package org.skellig.teststep.processor.rmq;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.rmq.model.RmqDetails;
import org.skellig.teststep.processor.rmq.model.RmqExchangeDetails;
import org.skellig.teststep.processor.rmq.model.RmqHostDetails;
import org.skellig.teststep.processor.rmq.model.RmqQueueDetails;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class RmqChannelCT {

    @Container
    private GenericContainer rabbitMqContainer =
            new GenericContainer(DockerImageName.parse("rabbitmq:latest"))
                    .withExposedPorts(5672);

    @AfterEach
    void tearDown() {
        rabbitMqContainer.close();
    }

    @Test
    @DisplayName("Send data to queue and read from it Then verify response is correct")
    void testSendAndRead() {
        String host = rabbitMqContainer.getHost();
        RmqChannel rmqChannel = new RmqChannel(createChannel(host));

        String data = "test";
        rmqChannel.send(data, "#");

        assertEquals(data, new String(rmqChannel.read(null, 100)));
    }

    private RmqDetails createChannel(String host) {
        return new RmqDetails.Builder()
                .withHostDetails(
                        new RmqHostDetails(host, rabbitMqContainer.getMappedPort(5672), "guest", "guest")
                )
                .withExchange(
                        new RmqExchangeDetails.Builder()
                                .withName("exchange1")
                                .withType("topic")
                                .withCreateIfNew(true)
                                .build()
                )
                .withQueue(
                        new RmqQueueDetails.Builder()
                                .withName("queue1")
                                .withDurable(true)
                                .withCreateIfNew(true)
                                .withRoutingKey("#")
                                .build()
                )
                .build();
    }
}