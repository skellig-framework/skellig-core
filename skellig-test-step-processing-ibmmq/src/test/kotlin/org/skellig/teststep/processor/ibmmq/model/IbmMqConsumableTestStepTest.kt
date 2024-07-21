package org.skellig.teststep.processor.ibmmq.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IbmMqConsumableTestStepTest {

    @Test
    fun `verify to string`() {
        assertEquals("name = t1\n" +
                "execution = ASYNC\n" +
                "consumeFrom = [c1]\n",
            IbmMqConsumableTestStep.Builder()
                .consumeFrom(listOf("c1"))
                .withName("t1")
                .build()
                .toString())

        assertEquals("name = t1\n" +
                "execution = ASYNC\n" +
                "consumeFrom = [c1]\n" +
                "respondTo = [c3]\n",
            IbmMqConsumableTestStep.Builder()
                .consumeFrom(listOf("c1"))
                .respondTo(listOf("c3"))
                .withName("t1")
                .build()
                .toString())
    }
}