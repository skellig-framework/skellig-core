package org.skellig.teststep.processor.jdbc

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.jdbc.model.JdbcDetails
import java.util.*

internal class JdbcDetailsConfigReaderTest {

    private var configReader: JdbcDetailsConfigReader? = null

    @BeforeEach
    fun setUp() {
        configReader = JdbcDetailsConfigReader()
    }

    @Test
    fun testReadJdbcConfig() {
        val details: List<JdbcDetails> = ArrayList(configReader!!.read(ConfigFactory.load("jdbc-test.conf")))
        Assertions.assertAll(
                { assertEquals(2, details.size) },
                { assertEquals("srv1", details[0].serverName) },
                { assertEquals("jdbc:/mysql", details[0].url) },
                { assertEquals("mysql driver class", details[0].driverName) },
                { assertEquals("usr1", details[0].userName ?: "") },
                { assertEquals("pswd1", details[0].password ?: "") },
                { assertEquals("srv2", details[1].serverName) },
                { assertEquals("jdbc:/mssql", details[1].url) },
                { assertEquals("mssql driver class", details[1].driverName) }
        )
    }
}