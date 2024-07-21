package org.skellig.teststep.processor.tcp.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TcpConsumableTestStepTest {

    @Test
    fun `verify to string`() {
        assertEquals("name = t1\n" +
                "execution = ASYNC\n" +
                "readBufferSize = 1048576\n" +
                "consumeFrom = [c1]\n",
            TcpConsumableTestStep.Builder()
                .consumeFrom(listOf("c1"))
                .withName("t1")
                .build()
                .toString())

        assertEquals("name = t1\n" +
                "execution = ASYNC\n" +
                "readBufferSize = 9000\n" +
                "consumeFrom = [c1]\n" +
                "respondTo = [c3]\n",
            TcpConsumableTestStep.Builder()
                .consumeFrom(listOf("c1"))
                .respondTo(listOf("c3"))
                .readBufferSize(9000)
                .withName("t1")
                .build()
                .toString())
    }
}