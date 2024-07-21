package org.skellig.teststep.processor.ibmmq.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IbmMqQueueDetailsTest {

    @Test
    fun `create queue details without mandatory fields`() {
        val ex = assertThrows<IllegalStateException> {
            IbmMqQueueDetails.Builder()
                .mqManagerDetails(
                    IbmMqManagerDetails.Builder()
                        .name("m1")
                        .host("localhost")
                        .port(1010)
                        .channel("chn1")
                        .build()
                )
                .id("id1")
                .build()
                .toString()
        }
        assertEquals("IBMMQ Queue name cannot be null", ex.message)

        val ex2 = assertThrows<IllegalStateException> {
            IbmMqQueueDetails.Builder()
                .name("n1")
                .id("id1")
                .build()
                .toString()
        }
        assertEquals("IBMMQ Queue manager cannot be null", ex2.message)

        val ex3 = assertThrows<IllegalStateException> {
            IbmMqQueueDetails.Builder()
                .mqManagerDetails(
                    IbmMqManagerDetails.Builder()
                        .host("localhost")
                        .port(1010)
                        .channel("chn1")
                        .build()
                )
                .id("id1")
                .name("q1")
                .build()
                .toString()
        }
        assertEquals("MQ Manager name cannot be null", ex3.message)

        val ex4 = assertThrows<IllegalStateException> {
            IbmMqQueueDetails.Builder()
                .mqManagerDetails(
                    IbmMqManagerDetails.Builder()
                        .name("n1")
                        .port(1010)
                        .channel("chn1")
                        .build()
                )
                .id("id1")
                .name("q1")
                .build()
                .toString()
        }
        assertEquals("MQ Manager host cannot be null", ex4.message)

        val ex5 = assertThrows<IllegalStateException> {
            IbmMqQueueDetails.Builder()
                .mqManagerDetails(
                    IbmMqManagerDetails.Builder()
                        .name("n1")
                        .host("h1")
                        .port(1010)
                        .build()
                )
                .id("id1")
                .name("q1")
                .build()
                .toString()
        }
        assertEquals("MQ Manager channel cannot be null", ex5.message)
    }

    @Test
    fun `verify to string`() {
        assertEquals(
            "(id = 'id1', queue = 'q1', IBMMQ Manager (name = 'm1', channel = 'chn1', host = 'localhost', port = 1010, userCredentials = (username = 'user A', password = 12345)))",
            IbmMqQueueDetails.Builder()
                .mqManagerDetails(
                    IbmMqManagerDetails.Builder()
                        .name("m1")
                        .host("localhost")
                        .port(1010)
                        .channel("chn1")
                        .userCredentials(IbmMqManagerDetails.IbmMqUserCredentials("user A", "12345"))
                        .build()
                )
                .id("id1")
                .name("q1")
                .build()
                .toString()
        )

        assertEquals(
            "(id = 'q1', queue = 'q1', IBMMQ Manager (name = 'm1', channel = 'chn1', host = 'localhost', port = 1010))",
            IbmMqQueueDetails.Builder()
                .mqManagerDetails(
                    IbmMqManagerDetails.Builder()
                        .name("m1")
                        .host("localhost")
                        .port(1010)
                        .channel("chn1")
                        .build()
                )
                .name("q1")
                .build()
                .toString()
        )
    }
}