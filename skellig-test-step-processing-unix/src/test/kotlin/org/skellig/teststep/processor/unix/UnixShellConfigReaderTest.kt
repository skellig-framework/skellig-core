package org.skellig.teststep.processor.unix;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UnixShellConfigReaderTest {

    private UnixShellConfigReader configReader;

    @BeforeEach
    void setUp() {
        configReader = new UnixShellConfigReader();
    }

    @Test
    void testReadUnixShellConfig() {
        List<UnixShellHostDetails> details = new ArrayList<>(configReader.read(ConfigFactory.load("unix-test.conf")));

        assertAll(
                () -> assertEquals(2, details.size()),
                () -> assertEquals("srv1", details.get(0).getHostName()),
                () -> assertEquals("localhost", details.get(0).getHostAddress()),
                () -> assertEquals(1010, details.get(0).getPort()),
                () -> assertEquals("~/.ssh/id_rsa", details.get(0).getSshKeyPath()),

                () -> assertEquals("srv2", details.get(1).getHostName()),
                () -> assertEquals("1.2.3.4", details.get(1).getHostAddress()),
                () -> assertEquals(22, details.get(1).getPort()),
                () -> assertEquals("usr1", details.get(1).getUserName()),
                () -> assertEquals("pswd1", details.get(1).getPassword())
        );
    }
}