package org.skellig.teststep.processor.ibmmq

import com.typesafe.config.ConfigFactory
import org.junit.Assert
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.ibmmq.model.IbmMqQueueDetails

internal class IbmmqDetailsConfigReaderTest {

    private var reader = IbmmqDetailsConfigReader()

    @Test
    @DisplayName("When null provided Then throw exception")
    fun testReadIbmmqDetailsWhenConfigIsNull() {
        val ex = assertThrows(NullPointerException::class.java) { reader.read(null) }

        Assert.assertEquals("IBMMQ config cannot be null", ex.message)
    }

    @Test
    @DisplayName("When valid config provided Then verify all read correctly")
    fun testReadIbmmqDetails() {
        val config = ConfigFactory.load("ibmmq-details.conf")

        val mqDetails = reader.read(config)

        assertAll(
                { assertEquals(3, mqDetails.size.toLong()) },
                {
                    assertTrue(mqDetails
                            .any { item: IbmMqQueueDetails ->
                                item.queueName == "client_A_CHN_1" &&
                                        item.ibmMqManagerDetails.name == "TEST_MQ1" &&
                                        item.ibmMqManagerDetails.channel == "TEST_CHANNEL_1" &&
                                        item.ibmMqManagerDetails.host == "localhost" &&
                                        item.ibmMqManagerDetails.port == 1421 &&
                                        item.ibmMqManagerDetails.userCredentials!!.username == "user1" &&
                                        item.ibmMqManagerDetails.userCredentials!!.password == "pswd1"
                            })
                },
                {
                    assertTrue(mqDetails
                            .any { item: IbmMqQueueDetails ->
                                item.queueName == "client_A_CHN_2" &&
                                        item.ibmMqManagerDetails.name == "TEST_MQ1"
                            })
                },
                {
                    assertTrue(mqDetails
                            .any { item: IbmMqQueueDetails ->
                                item.queueName == "client_A_CHN_3" &&
                                        item.ibmMqManagerDetails.name == "TEST_MQ2" &&
                                        item.ibmMqManagerDetails.channel == "TEST_CHANNEL_2" &&
                                        item.ibmMqManagerDetails.host == "localhost" &&
                                        item.ibmMqManagerDetails.port == 1422 &&
                                        item.ibmMqManagerDetails.userCredentials == null
                            })
                },
        )
    }
}