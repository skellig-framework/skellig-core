package org.skellig.teststep.processor.rmq.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RmqTestStepTest {

    @Test
    fun testToString() {
        assertEquals(
            "name = test a\n" +
                    "routingKey = key1\n" +
                    "sendTo = [q1]\n" +
                    "readFrom = [q2]\n" +
                    "respondTo = [q3]\n",
            RmqTestStep.Builder()
                .sendTo(setOf("q1"))
                .readFrom(setOf("q2"))
                .respondTo(setOf("q3"))
                .routingKey("key1")
                .withName("test a")
                .build()
                .toString()
        )
    }
}