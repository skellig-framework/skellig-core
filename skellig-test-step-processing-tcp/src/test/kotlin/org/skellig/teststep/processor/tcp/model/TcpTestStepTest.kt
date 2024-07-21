package org.skellig.teststep.processor.tcp.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TcpTestStepTest {

    @Test
    fun `verify to string`() {
        assertEquals("name = t1\n" +
                "readBufferSize = 1048576\n" +
                "sendTo = [c1]\n",
            TcpTestStep.Builder()
                .sendTo(setOf("c1"))
                .withName("t1")
                .build()
                .toString())

        assertEquals("name = t2\n" +
                "readBufferSize = 1000\n" +
                "sendTo = [c1]\n" +
                "readFrom = [c2]\n" +
                "respondTo = [c3]\n",
            TcpTestStep.Builder()
                .sendTo(setOf("c1"))
                .readFrom(setOf("c2"))
                .respondTo(setOf("c3"))
                .readBufferSize(1000)
                .withName("t2")
                .build()
                .toString())
    }
}