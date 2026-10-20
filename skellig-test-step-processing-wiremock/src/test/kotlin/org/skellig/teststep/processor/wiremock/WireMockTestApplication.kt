package org.skellig.teststep.processor.wiremock

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.skellig.teststep.runner.TestStepRunner
import org.skellig.teststep.runner.context.SkelligTestContext
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Builds a Skellig context from HOCON and `.sts` files, as the real runtime does, and plays the system under test
 * with a plain HTTP client. Later tests run scenarios through [run].
 */
internal class WireMockTestApplication(
    private val hocon: String,
    private val steps: List<String> = listOf("wiremock/steps.sts")
) {
    private val client = HttpClient.newHttpClient()

    fun run(test: (SkelligTestContext, TestStepRunner) -> Unit) {
        val config = ConfigFactory.parseString(hocon).resolve()
        val context = object : SkelligTestContext() {
            override fun createConfig(classLoader: ClassLoader, configPath: String?): Config = config
        }
        context.use { test(it, it.initialize(javaClass.classLoader, steps)) }
    }

    /** Runs a step and returns its result, failing the test with the step's error if it has one. */
    fun TestStepRunner.run(name: String, parameters: Map<String, Any?> = emptyMap()): Any? {
        var result: Any? = null
        var error: Throwable? = null
        run(name, parameters).subscribe { _, r, e -> result = r; error = e }
        error?.let { throw it }
        return result
    }

    fun run(runner: TestStepRunner, name: String): Throwable? {
        var error: Throwable? = null
        runner.run(name).subscribe { _, _, e -> error = e }
        return error
    }

    /** Calls the mock server the way the system under test would. */
    fun post(port: Int, path: String): HttpResponse<String> =
        client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path")).POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString())

    fun call(port: Int, path: String = "/"): HttpResponse<String> =
        client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port$path")).build(), HttpResponse.BodyHandlers.ofString())

    companion object {
        fun freePort(): Int = ServerSocket(0).use { it.localPort }

        fun isFree(port: Int): Boolean = runCatching { ServerSocket(port).use { } }.isSuccess
    }
}
