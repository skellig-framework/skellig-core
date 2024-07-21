package org.skellig.teststep.processor.ibmmq.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IbmMqTestStepTest {

    @Test
    fun `verify to string`() {
        assertEquals("name = t1\n" +
                "sendTo = [c1]\n",
            IbmMqTestStep.Builder()
                .sendTo(setOf("c1"))
                .withName("t1")
                .build()
                .toString())

        assertEquals("name = t1\n" +
                "sendTo = [c1]\n" +
                "readFrom = [c2]\n" +
                "respondTo = [c3]\n",
            IbmMqTestStep.Builder()
                .sendTo(setOf("c1"))
                .readFrom(setOf("c2"))
                .respondTo(setOf("c3"))
                .withName("t1")
                .build()
                .toString())
    }
}