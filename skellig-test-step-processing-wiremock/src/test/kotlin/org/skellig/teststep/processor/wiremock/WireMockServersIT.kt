package org.skellig.teststep.processor.wiremock

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.processor.config.TestStepProcessorConfigDetails
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.freePort
import org.skellig.teststep.processor.wiremock.WireMockTestApplication.Companion.isFree
import org.skellig.teststep.processor.wiremock.config.WireMockTestStepProcessorConfig
import java.net.ServerSocket

@Tag("integration-test")
class WireMockServersIT {

    private fun servers(vararg entries: String) = "wiremock { servers = [ ${entries.joinToString()} ] }"

    private fun failureMessages(app: WireMockTestApplication): String {
        val error = assertThrows(Exception::class.java) { app.run { _, _ -> } }
        return generateSequence<Throwable>(error) { it.cause }.joinToString(" | ") { it.message.orEmpty() }
    }

    @Test
    fun `configured servers accept connections once the context is created and free their ports on close`() {
        val first = freePort()
        val second = freePort()
        val app = WireMockTestApplication(servers("""{ name = payments, port = $first }""", """{ name = identity, port = $second, journal { max-entries = 5 } }"""))

        app.run { _, _ ->
            assertEquals(404, app.call(first).statusCode())
            assertEquals(404, app.call(second).statusCode())
            assertFalse(isFree(first))
        }

        assertTrue(isFree(first), "Port $first still in use after close")
        assertTrue(isFree(second), "Port $second still in use after close")
    }

    @Test
    fun `duplicate names fail at startup`() {
        val messages = failureMessages(WireMockTestApplication(servers("""{ name = a, port = ${freePort()} }""", """{ name = a, port = ${freePort()} }""")))
        assertTrue(messages.contains("mock server name 'a' more than once"), messages)
    }

    @Test
    fun `duplicate ports fail at startup`() {
        val port = freePort()
        val messages = failureMessages(WireMockTestApplication(servers("""{ name = a, port = $port }""", """{ name = b, port = $port }""")))
        assertTrue(messages.contains("port $port more than once") && messages.contains("'a'") && messages.contains("'b'"), messages)
    }

    @Test
    fun `port 0 fails at startup`() {
        val messages = failureMessages(WireMockTestApplication(servers("""{ name = a, port = 0 }""")))
        assertTrue(messages.contains("wiremock server 'a' has invalid port 0"), messages)
    }

    @Test
    fun `a port already in use fails startup naming the server and port and stops earlier servers`() {
        val free = freePort()
        ServerSocket(0).use { busy ->
            val messages = failureMessages(WireMockTestApplication(servers("""{ name = ok, port = $free }""", """{ name = taken, port = ${busy.localPort} }""")))
            assertTrue(messages.contains("'taken'") && messages.contains("port ${busy.localPort}"), messages)
        }
        assertTrue(isFree(free), "Server started before the failure was left running")
    }

    @Test
    fun `no wiremock key means no processor is registered`() {
        val details = TestStepProcessorConfigDetails(mock(), ConfigFactory.parseString("ui { }"), mock(), mock(), mock(), mock())
        assertNull(WireMockTestStepProcessorConfig().config(details))
    }
}
