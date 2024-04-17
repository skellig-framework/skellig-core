package org.skellig.teststep.processor.jdbc

import com.typesafe.config.Config
import org.skellig.teststep.processor.jdbc.model.JdbcDetails
import java.util.*

/**
 * The JdbcDetailsConfigReader class is responsible for reading JDBC configuration from a Skellig [Config] file,
 * defined in 'jdbc.servers' property, and returning a collection of [JdbcDetails] objects.
 */
internal class JdbcDetailsConfigReader {

    companion object {
        private const val JDBC_CONFIG_KEYWORD = "jdbc.servers"
    }

    fun read(config: Config): Collection<JdbcDetails> {
        Objects.requireNonNull(config, "JDBC config cannot be null")
        var jdbcDetails = emptyList<JdbcDetails>()

        if (config.hasPath(JDBC_CONFIG_KEYWORD)) {
            val anyRefList = config.getAnyRefList(JDBC_CONFIG_KEYWORD) as List<*>
            jdbcDetails = anyRefList
                .mapNotNull { createJdbcDetails(it) }
                .toList()
        }
        return jdbcDetails
    }

    private fun createJdbcDetails(rawJdbcDetails: Any?): JdbcDetails? {
        return (rawJdbcDetails as Map<*, *>?)?.let {
            val server = it["server"] as String?
            val url = it["url"] as String?
            val driver = it["driver"] as String?
            val userName = it["userName"] as String?
            val password = it["password"] as String?

            Objects.requireNonNull(server, "Server name must be declared for JDBC instance")
            Objects.requireNonNull(url, "Url name must be declared for JDBC instance")
            Objects.requireNonNull(driver, "Driver class name must be declared for JDBC instance")

            JdbcDetails(server!!, driver!!, url!!, userName, password)
        }
    }
}