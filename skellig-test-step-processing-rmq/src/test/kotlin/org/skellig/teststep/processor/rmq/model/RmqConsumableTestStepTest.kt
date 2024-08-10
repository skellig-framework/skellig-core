package org.skellig.teststep.processor.rmq.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RmqConsumableTestStepTest  {

    @Test
    fun testToString() {
        assertEquals(
            "name = test a\n" +
                    "execution = ASYNC\n" +
                    "routingKey = key1\n" +
                    "consumeFrom = [q1]\n" +
                    "respondTo = [q3]\n",
            RmqConsumableTestStep.Builder()
                .consumeFrom(listOf("q1"))
                .respondTo(listOf("q3"))
                .routingKey("key1")
                .withName("test a")
                .build()
                .toString()
        )
    }
}