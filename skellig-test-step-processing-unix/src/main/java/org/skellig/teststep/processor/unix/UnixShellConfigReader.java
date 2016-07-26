package org.skellig.teststep.processor.unix;

import com.typesafe.config.Config;
import org.skellig.teststep.processor.unix.model.UnixShellHostDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UnixShellConfigReader {

    private static final String UNIX_SHELL_CONFIG_KEYWORD = "unix-shell";

    Collection<UnixShellHostDetails> read(Config config) {
        Objects.requireNonNull(config, "Unix Shell config cannot be null");

        Collection<UnixShellHostDetails> unixShellDetails = Collections.emptyList();
        if (config.hasPath(UNIX_SHELL_CONFIG_KEYWORD)) {
            List<Map> anyRefList = (List<Map>) config.getAnyRefList(UNIX_SHELL_CONFIG_KEYWORD);
            unixShellDetails = anyRefList.stream()
                    .map(this::createUnixShellDetails)
                    .collect(Collectors.toList());
        }
        return unixShellDetails;
    }

    private UnixShellHostDetails createUnixShellDetails(Map rawJdbcDetails) {
        String hostName = (String) rawJdbcDetails.get("hostName");
        String hostAddress = (String) rawJdbcDetails.get("hostAddress");
        Integer port = (Integer) rawJdbcDetails.getOrDefault("port", 22);
        String userName = (String) rawJdbcDetails.get("userName");
        String password = (String) rawJdbcDetails.get("password");
        String sshKeyPath = (String) rawJdbcDetails.get("sshKeyPath");

        Objects.requireNonNull(hostName, "Server name must be declared for JDBC instance");
        Objects.requireNonNull(hostAddress, "Driver class name must be declared for JDBC instance");

        return new UnixShellHostDetails.Builder()
                .withHostName(hostName)
                .withHostAddress(hostAddress)
                .withPort(port)
                .withUserName(userName)
                .withPassword(password)
                .withSshKeyPath(sshKeyPath)
                .build();
    }

}
