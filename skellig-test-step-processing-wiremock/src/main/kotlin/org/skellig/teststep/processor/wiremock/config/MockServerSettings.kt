package org.skellig.teststep.processor.wiremock.config

import com.typesafe.config.Config

internal data class MockServerSettings(val name: String, val port: Int, val journalMaxEntries: Int?) {

    companion object {
        /** Reads and validates `wiremock.servers`; fails with a message naming the offending entry. */
        fun readAll(wiremock: Config): List<MockServerSettings> {
            require(wiremock.hasPath("servers")) { "wiremock.servers must declare at least one mock server" }

            val servers = wiremock.getConfigList("servers").mapIndexed { index, server -> read(server, index + 1) }
            require(servers.isNotEmpty()) { "wiremock.servers must declare at least one mock server" }

            servers.groupBy { it.name }.filterValues { it.size > 1 }.keys.firstOrNull()
                ?.let { throw IllegalArgumentException("wiremock.servers declares mock server name '$it' more than once") }
            servers.groupBy { it.port }.filterValues { it.size > 1 }.entries.firstOrNull()
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

            return MockServerSettings(name, port, maxEntries)
        }
    }
}
