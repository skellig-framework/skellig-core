package org.skellig.teststep.processor.jdbc;

import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processor.jdbc.model.JdbcDetails;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcDetailsConfigReaderTest {

    private JdbcDetailsConfigReader configReader;

    @BeforeEach
    void setUp() {
        configReader = new JdbcDetailsConfigReader();
    }

    @Test
    void testReadJdbcConfig() {
        List<JdbcDetails> details = new ArrayList<>(configReader.read(ConfigFactory.load("jdbc-test.conf")));

        assertAll(
                () -> assertEquals(2, details.size()),
                () -> assertEquals("srv1", details.get(0).getServerName()),
                () -> assertEquals("jdbc:/mysql", details.get(0).getUrl()),
                () -> assertEquals("mysql driver class", details.get(0).getDriverName()),
                () -> assertEquals("usr1", details.get(0).getUserName().orElse("")),
                () -> assertEquals("pswd1", details.get(0).getPassword().orElse("")),

                () -> assertEquals("srv2", details.get(1).getServerName()),
                () -> assertEquals("jdbc:/mssql", details.get(1).getUrl()),
                () -> assertEquals("mssql driver class", details.get(1).getDriverName())
        );
    }
}