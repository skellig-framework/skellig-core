package org.skellig.teststep.processor.cassandra;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.cassandra.model.CassandraDetails;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CassandraDetailsConfigReaderTest {

    private CassandraDetailsConfigReader configReader;

    @BeforeEach
    void setUp() {
        configReader = new CassandraDetailsConfigReader();
    }

    @Test
    void testReadJdbcConfig() {
        List<CassandraDetails> details = new ArrayList<>(configReader.read(ConfigFactory.load("cassandra-test.conf")));

        assertAll(
                () -> assertEquals(2, details.size()),
                () -> assertEquals("srv1", details.get(0).getServerName()),
                () -> assertEquals(2, details.get(0).getNodes().size()),
                () -> assertEquals("0.0.0.10", new ArrayList<>(details.get(0).getNodes()).get(0).getHostName()),
                () -> assertEquals(1001, new ArrayList<>(details.get(0).getNodes()).get(0).getPort()),
                () -> assertEquals("0.0.0.11", new ArrayList<>(details.get(0).getNodes()).get(1).getHostName()),
                () -> assertEquals(1002, new ArrayList<>(details.get(0).getNodes()).get(1).getPort()),
                () -> assertEquals("usr1", details.get(0).getUserName().orElse("")),
                () -> assertEquals("pswd1", details.get(0).getPassword().orElse("")),

                () -> assertEquals("srv2", details.get(1).getServerName()),
                () -> assertEquals(1, details.get(1).getNodes().size()),
                () -> assertEquals("localhost", new ArrayList<>(details.get(1).getNodes()).get(0).getHostName()),
                () -> assertEquals(3003, new ArrayList<>(details.get(1).getNodes()).get(0).getPort())
        );
    }
}