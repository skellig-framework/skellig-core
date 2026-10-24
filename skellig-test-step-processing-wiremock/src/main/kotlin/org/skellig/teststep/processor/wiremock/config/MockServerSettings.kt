package org.skellig.teststep.processor.wiremock.config

import com.typesafe.config.Config
import java.io.File
import java.net.InetAddress

internal data class MockServerSettings(
    val name: String,
    val port: Int,
    val journalMaxEntries: Int? = null,
    val bindAddress: String? = null,
    val filesDirectory: File? = null,
    val https: HttpsSettings? = null
) {

    internal data class HttpsSettings(val port: Int, val keystorePath: String, val keystorePassword: String, val keystoreType: String) {
        override fun toString() = "HttpsSettings(port=$port, keystorePath=$keystorePath, keystoreType=$keystoreType)"
    }

    companion object {
        /** Reads and validates `wiremock.servers`; fails with a message naming the offending entry. */
        fun readAll(wiremock: Config): List<MockServerSettings> {
            require(wiremock.hasPath("servers")) { "wiremock.servers must declare at least one mock server" }

            val servers = wiremock.getConfigList("servers").mapIndexed { index, server -> read(server, index + 1) }
            require(servers.isNotEmpty()) { "wiremock.servers must declare at least one mock server" }

            servers.groupBy { it.name }.filterValues { it.size > 1 }.keys.firstOrNull()
                ?.let { throw IllegalArgumentException("wiremock.servers declares mock server name '$it' more than once") }
            servers.flatMap { server -> listOfNotNull(server.port, server.https?.port).map { it to server } }
                .groupBy({ it.first }, { it.second }).filterValues { it.size > 1 }.entries.firstOrNull()
                ?.let { (port, same) ->
                    throw IllegalArgumentException("wiremock.servers declares port $port more than once: ${same.joinToString { "'${it.name}'" }}")
                }
            return servers
        }

        private fun read(server: Config, position: Int): MockServerSettings {
            require(server.hasPath("name") && server.getString("name").isNotBlank()) {
                "wiremock.servers entry $position must have a non-blank name"
            }
            val name = server.getString("name")
            require(server.hasPath("port")) { "wiremock server '$name' must have a fixed port" }

            val port = server.getInt("port")
            require(port in 1..65535) { "wiremock server '$name' has invalid port $port" }

            val maxEntries = if (server.hasPath("journal.max-entries")) server.getInt("journal.max-entries") else null
            require(maxEntries == null || maxEntries >= 1) { "wiremock server '$name' has invalid journal.max-entries $maxEntries; it must be at least 1" }

            return MockServerSettings(name, port, maxEntries, readBindAddress(server, name), readFilesDirectory(server, name), readHttps(server, name, port))
        }

        private fun readBindAddress(server: Config, name: String): String? {
            if (!server.hasPath("bind-address")) return null
            val address = server.getString("bind-address")
            require(address.isNotBlank()) { "wiremock server '$name' has a blank bind-address" }
            try {
                InetAddress.getByName(address)
            } catch (ex: Exception) {
                throw IllegalArgumentException("wiremock server '$name' has invalid bind-address '$address'", ex)
            }
            return address
        }

        private fun readFilesDirectory(server: Config, name: String): File? {
            if (!server.hasPath("files-directory")) return null
            val directory = File(server.getString("files-directory"))
            require(directory.isDirectory) { "wiremock server '$name' has files-directory '$directory' which is not an existing directory" }
            return directory
        }

        private fun readHttps(server: Config, name: String, httpPort: Int): HttpsSettings? {
            if (!server.hasPath("https")) return null
            val https = server.getConfig("https")
            require(https.hasPath("port")) { "wiremock server '$name' https must have a port" }
            val port = https.getInt("port")
            require(port in 1..65535) { "wiremock server '$name' has invalid https port $port" }
            require(port != httpPort) { "wiremock server '$name' uses port $port for both http and https" }

            require(https.hasPath("keystore-path") && https.getString("keystore-path").isNotBlank()) {
                "wiremock server '$name' https must have a keystore-path"
            }
            val keystorePath = https.getString("keystore-path")
            require(File(keystorePath).isFile) { "wiremock server '$name' has https keystore-path '$keystorePath' which is not an existing file" }
            require(https.hasPath("keystore-password")) { "wiremock server '$name' https must have a keystore-password" }

            val defaultType = if (keystorePath.lowercase().let { it.endsWith(".p12") || it.endsWith(".pfx") }) "PKCS12" else "JKS"
            val type = if (https.hasPath("keystore-type")) https.getString("keystore-type") else defaultType

            return HttpsSettings(port, keystorePath, https.getString("keystore-password"), type)
        }
    }
}
