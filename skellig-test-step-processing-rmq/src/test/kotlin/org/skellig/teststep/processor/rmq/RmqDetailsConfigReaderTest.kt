package org.skellig.teststep.processor.rmq

import com.typesafe.config.ConfigFactory
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.skellig.teststep.processor.rmq.model.RmqDetails

@DisplayName("Read RMQ details")
internal class RmqDetailsConfigReaderTest {

    private var rmqDetailsConfigReader = RmqDetailsConfigReader()

    @Test
    @DisplayName("xWhen null provided Then throw exception")
    fun testReadRmqDetailsWhenConfigIsNull() {
        val ex = Assertions.assertThrows(NullPointerException::class.java) { rmqDetailsConfigReader.read(null) }

        Assert.assertEquals("RMQ config cannot be null", ex.message)
    }

    @Test
    @DisplayName("When valid config provided Then verify all read correctly")
    fun testReadRmqDetails() {
        val config = ConfigFactory.load("rmq-details.conf")

        val mqDetails = rmqDetailsConfigReader.read(config)

        assertAll(
                { assertEquals(3, mqDetails.size.toLong()) },
                {
                    assertTrue(mqDetails
                            .any { item: RmqDetails? ->
                                val hostDetails = item!!.hostDetails
                                item.exchange.name == "exchange1" && item.exchange.type == "topic" &&
                                        item.exchange.isCreateIfNew &&
                                        item.exchange.isAutoDelete &&
                                        item.exchange.isDurable && item.channelId == "Q1" && item.queue.name == "queue1" && item.queue.routingKey == "any" &&
                                        item.queue.isCreateIfNew &&
                                        item.queue.isAutoDelete &&
                                        item.queue.isDurable &&
                                        item.queue.isExclusive && hostDetails.host == "localhost" && hostDetails.port == 5672 && hostDetails.user == "usr1" && hostDetails.password == "pswd1"
                            })
                },
                {
                    assertTrue(mqDetails
                            .any { item: RmqDetails? ->
                                val hostDetails = item!!.hostDetails
                                item.channelId == "Q2" && item.queue.name == "queue1" && item.queue.routingKey == "#" &&
                                        !item.queue.isCreateIfNew &&
                                        !item.queue.isAutoDelete &&
                                        !item.queue.isDurable &&
                                        !item.queue.isExclusive && item.exchange.name == "exchange2" && hostDetails.host == "localhost" && hostDetails.port == 5673 && hostDetails.user == "usr2" && hostDetails.password == "pswd2"
                            })
                },
                {
                    assertTrue(mqDetails
                            .any { item: RmqDetails? ->
                                val hostDetails = item!!.hostDetails
                                item.channelId == "Q3" && item.queue.name == "queue2" && item.exchange.name == "exchange2" && item.exchange.type == null && hostDetails.host == "localhost" && hostDetails.port == 5673 && hostDetails.user == "usr2" && hostDetails.password == "pswd2"
                            })
                }
        )
    }
}