package org.skellig.teststep.processor.jdbc

import com.typesafe.config.Config
import org.skellig.teststep.processor.jdbc.model.JdbcDetails
import java.util.*

internal class JdbcDetailsConfigReader {

    companion object {
        private const val JDBC_CONFIG_KEYWORD = "jdbc"
    }

    fun read(config: Config): Collection<JdbcDetails> {
        Objects.requireNonNull(config, "JDBC config cannot be null")
        var jdbcDetails = emptyList<JdbcDetails>()

        if (config.hasPath(JDBC_CONFIG_KEYWORD)) {
            val anyRefList = config.getAnyRefList(JDBC_CONFIG_KEYWORD) as List<Map<*, *>>
            jdbcDetails = anyRefList
                    .map { createJdbcDetails(it) }
                    .toList()
        }
        return jdbcDetails
    }

    private fun createJdbcDetails(rawJdbcDetails: Map<*, *>): JdbcDetails {
        val server = rawJdbcDetails["server"] as String?
        val url = rawJdbcDetails["url"] as String?
        val driver = rawJdbcDetails["driver"] as String?
        val userName = rawJdbcDetails["userName"] as String?
        val password = rawJdbcDetails["password"] as String?

        Objects.requireNonNull(server, "Server name must be declared for JDBC instance")
        Objects.requireNonNull(url, "Url name must be declared for JDBC instance")
        Objects.requireNonNull(driver, "Driver class name must be declared for JDBC instance")

        return JdbcDetails(server!!, driver!!, url!!, userName, password)
    }
}