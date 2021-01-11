package org.skellig.teststep.processor.unix

import com.typesafe.config.ConfigFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails
import java.util.*

internal class UnixShellConfigReaderTest {

    private var configReader: UnixShellConfigReader? = null

    @BeforeEach
    fun setUp() {
        configReader = UnixShellConfigReader()
    }

    @Test
    fun testReadUnixShellConfig() {
        val details: List<UnixShellHostDetails> = ArrayList(configReader!!.read(ConfigFactory.load("unix-test.conf")))

        Assertions.assertAll(
                { Assertions.assertEquals(2, details.size) },
                { Assertions.assertEquals("srv1", details[0].hostName) },
                { Assertions.assertEquals("localhost", details[0].hostAddress) },
                { Assertions.assertEquals(1010, details[0].port) },
                { Assertions.assertEquals("~/.ssh/id_rsa", details[0].sshKeyPath) },
                { Assertions.assertEquals("srv2", details[1].hostName) },
                { Assertions.assertEquals("1.2.3.4", details[1].hostAddress) },
                { Assertions.assertEquals(22, details[1].port) },
                { Assertions.assertEquals("usr1", details[1].userName) },
                { Assertions.assertEquals("pswd1", details[1].password) }
        )
    }
}