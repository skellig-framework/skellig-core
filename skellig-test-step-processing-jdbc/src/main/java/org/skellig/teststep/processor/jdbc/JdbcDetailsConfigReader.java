package org.skellig.teststep.processor.jdbc;

import com.typesafe.config.Config;
import org.skellig.teststep.processor.jdbc.model.JdbcDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class JdbcDetailsConfigReader {

    private static final String JDBC_CONFIG_KEYWORD = "jdbc";

    Collection<JdbcDetails> read(Config config) {
        Objects.requireNonNull(config, "JDBC config cannot be null");

        Collection<JdbcDetails> jdbcDetails = Collections.emptyList();
        if (config.hasPath(JDBC_CONFIG_KEYWORD)) {
            List<Map> anyRefList = (List<Map>) config.getAnyRefList(JDBC_CONFIG_KEYWORD);
            jdbcDetails = anyRefList.stream()
                    .map(this::createCasJdbcDetails)
                    .collect(Collectors.toList());
        }
        return jdbcDetails;
    }

    private JdbcDetails createCasJdbcDetails(Map rawJdbcDetails) {
        String server = (String) rawJdbcDetails.get("server");
        String url = (String) rawJdbcDetails.get("url");
        String driver = (String) rawJdbcDetails.get("driver");
        String userName = (String) rawJdbcDetails.get("userName");
        String password = (String) rawJdbcDetails.get("password");

        Objects.requireNonNull(server, "Server name must be declared for JDBC instance");
        Objects.requireNonNull(url, "Url name must be declared for JDBC instance");
        Objects.requireNonNull(driver, "Driver class name must be declared for JDBC instance");

        return new JdbcDetails(server, driver, url, userName, password);
    }

}
