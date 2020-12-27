package org.skellig.teststep.processor.rmq

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.rmq.model.RmqDetails
import org.skellig.teststep.processor.rmq.model.RmqExchangeDetails
import org.skellig.teststep.processor.rmq.model.RmqHostDetails
import org.skellig.teststep.processor.rmq.model.RmqQueueDetails
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
internal class RmqChannelCT {
    @Container
    private val rabbitMqContainer =
            GenericContainer<GenericContainer<*>>(DockerImageName.parse("rabbitmq:latest"))
                    .withExposedPorts(5672)

    @AfterEach
    fun tearDown() {
        rabbitMqContainer.close()
    }

    @Test
    @DisplayName("Send data to queue and read from it Then verify response is correct")
    fun testSendAndRead() {
        val host = rabbitMqContainer.host
        val rmqChannel = RmqChannel(createChannel(host))
        val data = "test"

        rmqChannel.send(data, "#")

        Assertions.assertEquals(data, String(rmqChannel.read(null, 100)!!))
    }

    private fun createChannel(host: String): RmqDetails {
        return RmqDetails.Builder()
                .withChannelId("chn_1")
                .withHostDetails(
                        RmqHostDetails(host, rabbitMqContainer.getMappedPort(5672), "guest", "guest")
                )
                .withExchange(
                        RmqExchangeDetails.Builder()
                                .withName("exchange1")
                                .withType("topic")
                                .withCreateIfNew(true)
                                .build()
                )
                .withQueue(
                        RmqQueueDetails.Builder()
                                .withName("queue1")
                                .withDurable(true)
                                .withCreateIfNew(true)
                                .withRoutingKey("#")
                                .build()
                )
                .build()
    }
}