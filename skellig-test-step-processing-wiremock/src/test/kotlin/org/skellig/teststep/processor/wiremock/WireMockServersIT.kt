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
import java.io.File
import java.net.ConnectException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import org.junit.jupiter.api.Assumptions.assumeTrue

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

    private val keystore = File("src/test/resources/wiremock/test-keystore.p12").absolutePath

    private fun trustingClient(): HttpClient {
        val store = KeyStore.getInstance("PKCS12").also { File(keystore).inputStream().use { input -> it.load(input, "changeit".toCharArray()) } }
        val trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).also { it.init(store) }
        return HttpClient.newBuilder().sslContext(SSLContext.getInstance("TLS").also { it.init(null, trust.trustManagers, null) }).build()
    }

    @Test
    fun `an https call to a server with a keystore succeeds`() {
        val http = freePort()
        val https = freePort()
        val app = WireMockTestApplication(servers("""{ name = secure, port = $http, https { port = $https, keystore-path = "$keystore", keystore-password = changeit } }"""))

        app.run { _, _ ->
            val response = trustingClient().send(HttpRequest.newBuilder(URI.create("https://localhost:$https/")).build(), HttpResponse.BodyHandlers.ofString())
            assertEquals(404, response.statusCode())
            assertEquals(404, app.call(http).statusCode())
        }
        assertTrue(isFree(https), "https port $https still in use after close")
    }

    @Test
    fun `an https port equal to another server port fails at startup`() {
        val port = freePort()
        val messages = failureMessages(WireMockTestApplication(servers(
            """{ name = a, port = $port }""",
            """{ name = b, port = ${freePort()}, https { port = $port, keystore-path = "$keystore", keystore-password = changeit } }"""
        )))
        assertTrue(messages.contains("port $port more than once"), messages)
    }

    @Test
    fun `invalid https settings fail at startup with clear messages`() {
        fun https(body: String) = failureMessages(WireMockTestApplication(servers("""{ name = a, port = ${freePort()}, https { $body } }""")))

        assertTrue(https("keystore-path = \"$keystore\", keystore-password = x").contains("wiremock server 'a' https must have a port"))
        assertTrue(https("port = 70000, keystore-path = \"$keystore\", keystore-password = x").contains("invalid https port 70000"))
        assertTrue(https("port = ${freePort()}, keystore-password = x").contains("https must have a keystore-path"))
        assertTrue(https("port = ${freePort()}, keystore-path = /no/such.p12, keystore-password = x").contains("keystore-path '/no/such.p12' which is not an existing file"))
        assertTrue(https("port = ${freePort()}, keystore-path = \"$keystore\"").contains("https must have a keystore-password"))
    }

    @Test
    fun `a wrong keystore password fails startup naming the server`() {
        val messages = failureMessages(WireMockTestApplication(servers(
            """{ name = a, port = ${freePort()}, https { port = ${freePort()}, keystore-path = "$keystore", keystore-password = wrong } }"""
        )))
        assertTrue(messages.contains("Failed to start wiremock server 'a'"), messages)
    }

    @Test
    fun `file based mappings from the files directory are served`() {
        val dir = Files.createTempDirectory("wiremock-files").toFile()
        try {
            File(dir, "mappings").mkdirs()
            File(dir, "__files").mkdirs()
            File(dir, "__files/greeting.txt").writeText("hello from file")
            File(dir, "mappings/greeting.json").writeText(
                """{"request":{"method":"GET","urlPath":"/greeting"},"response":{"status":200,"bodyFileName":"greeting.txt"}}"""
            )
            val port = freePort()
            WireMockTestApplication(servers("""{ name = files, port = $port, files-directory = "${dir.path}" }""")).run { _, _ ->
                val response = WireMockTestApplication("").call(port, "/greeting")
                assertEquals(200, response.statusCode())
                assertEquals("hello from file", response.body())
            }
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `a missing files directory fails at startup`() {
        val messages = failureMessages(WireMockTestApplication(servers("""{ name = a, port = ${freePort()}, files-directory = "/no/such/dir" }""")))
        assertTrue(messages.contains("wiremock server 'a' has files-directory '/no/such/dir' which is not an existing directory"), messages)
    }

    @Test
    fun `the bind address is honoured`() {
        val lan = NetworkInterface.getNetworkInterfaces().asSequence().filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.asSequence() }.filterIsInstance<Inet4Address>().firstOrNull()
        assumeTrue(lan != null, "no non-loopback IPv4 address to prove the bind address is honoured")
        val port = freePort()

        WireMockTestApplication(servers("""{ name = local, port = $port, bind-address = "127.0.0.1" }""")).run { _, app ->
            assertEquals(404, WireMockTestApplication("").call(port).statusCode())
            assertThrows(ConnectException::class.java) {
                HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://${lan!!.hostAddress}:$port/")).build(), HttpResponse.BodyHandlers.ofString())
            }
        }
    }

    @Test
    fun `an invalid bind address fails at startup`() {
        val messages = failureMessages(WireMockTestApplication(servers("""{ name = a, port = ${freePort()}, bind-address = "not a host!" }""")))
        assertTrue(messages.contains("wiremock server 'a' has invalid bind-address 'not a host!'"), messages)
    }

    @Test
    fun `the journal cap limits retained requests`() {
        val port = freePort()
        val app = WireMockTestApplication(servers("""{ name = capped, port = $port, journal { max-entries = 2 } }"""), listOf("wiremock/requests.sts"))
        app.run { _, runner ->
            repeat(5) { WireMockTestApplication("").call(port, "/r$it") }
            val result = with(app) { runner.runStep("Read journal") } as Map<*, *>
            assertEquals(2, result["count"])
        }
    }

    @Test
    fun `an invalid journal cap fails at startup`() {
        val messages = failureMessages(WireMockTestApplication(servers("""{ name = a, port = ${freePort()}, journal { max-entries = 0 } }""")))
        assertTrue(messages.contains("wiremock server 'a' has invalid journal.max-entries 0"), messages)
    }
}
