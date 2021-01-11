package org.skellig.teststep.processor.cassandra

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.cassandra.model.CassandraDetails
import java.util.*

internal class CassandraDetailsConfigReaderTest {

    private var configReader = CassandraDetailsConfigReader()


    @Test
    fun testReadJdbcConfig() {
        val details: List<CassandraDetails> = ArrayList(configReader.read(ConfigFactory.load("cassandra-test.conf")))
        Assertions.assertAll(
                { Assertions.assertEquals(2, details.size) },
                { Assertions.assertEquals("srv1", details[0].serverName) },
                { Assertions.assertEquals(2, details[0].nodes.size) },
                { Assertions.assertEquals("0.0.0.10", ArrayList(details[0].nodes)[0].hostName) },
                { Assertions.assertEquals(1001, ArrayList(details[0].nodes)[0].port) },
                { Assertions.assertEquals("0.0.0.11", ArrayList(details[0].nodes)[1].hostName) },
                { Assertions.assertEquals(1002, ArrayList(details[0].nodes)[1].port) },
                { Assertions.assertEquals("usr1", details[0].userName ?: "") },
                { Assertions.assertEquals("pswd1", details[0].password ?: "") },
                { Assertions.assertEquals("srv2", details[1].serverName) },
                { Assertions.assertEquals(1, details[1].nodes.size) },
                { Assertions.assertEquals("localhost", ArrayList(details[1].nodes)[0].hostName) },
                { Assertions.assertEquals(3003, ArrayList(details[1].nodes)[0].port) }
        )
    }
}